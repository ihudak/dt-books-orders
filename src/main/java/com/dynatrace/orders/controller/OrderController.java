package com.dynatrace.orders.controller;

import com.dynatrace.orders.exception.*;
import com.dynatrace.orders.model.*;
import com.dynatrace.orders.repository.*;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
public class OrderController {
    @Value("${added.workload.cpu}")
    private long cpuPressure;
    @Value("${added.workload.ram}")
    private int memPressureMb;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    StorageRepository storageRepository;
    @Autowired
    PaymentRepository paymentRepository;
    Logger logger = LoggerFactory.getLogger(OrderController.class);


    // get all Orders
    @GetMapping("/orders")
    public List<Order> getAllCarts() {
        return orderRepository.findAll(Sort.by(Sort.Direction.ASC, "email", "createdAt"));
    }

    // get Order by ID
    @GetMapping("/orders/{id}")
    public Order getCartById(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order.get();
    }

    // get Orders of a user
    @GetMapping("/orders/findByEmail")
    public List<Order> getCartsByEmail(@RequestParam String email) {
        this.verifyClient(email);
        return orderRepository.findByEmail(email);
    }

    // get all users who ordered the book
    @GetMapping("/orders/findByISBN")
    public List<Order> getCartsByISBN(@RequestParam String isbn) {
        this.verifyBook(isbn);
        return orderRepository.findByEmail(isbn);
    }

    // create an order
    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        Book book = verifyBook(order.getIsbn());
        verifyClient(order.getEmail());
        Storage storage = verifyStorage(order.getIsbn(), order.getQuantity());
        if (order.isCompleted()) {
            buyFromStorage(storage, order, book);
        }
        return orderRepository.save(order);
    }

    // update an order
    @PutMapping("/orders/{id}")
    public Order updateOrderById(@PathVariable Long id, @RequestBody Order order) {
        Optional<Order> orderDb = orderRepository.findById(id);
        if (orderDb.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        } else if (order.getId() != id || orderDb.get().getId() != id) {
            throw new BadRequestException("bad order id");
        }

        Storage storage = verifyStorage(order.getIsbn(), order.getQuantity());
        if (order.isCompleted() && !orderDb.get().isCompleted()) {
            Book book = verifyBook(order.getIsbn());
            // complete the order
            buyFromStorage(storage, order, book);
        } else if (!order.isCompleted() && orderDb.get().isCompleted()) {
            // cancel the order
            returnToStorage(storage, order);
        }

        return orderRepository.save(order);
    }

    // submit order
    @PostMapping("/orders/submit")
    public Order submitOrder(@RequestBody Order order) {
        Order orderDb = orderRepository.findByEmailAndIsbn(order.getEmail(), order.getIsbn());
        if (null == orderDb) {
            throw new BadRequestException("Order not found, ISBN " + order.getIsbn() + " client " + order.getEmail());
        }
        if (orderDb.isCompleted()) {
            throw new PurchaseForbiddenException("Order is already paid, ISBN " + order.getIsbn() + " client " + order.getEmail());
        }
        verifyClient(order.getEmail());
        Book book = verifyBook(order.getIsbn());
        Storage storage = verifyStorage(order.getIsbn(), order.getQuantity());
        orderDb.setQuantity(order.getQuantity());

        buyFromStorage(storage, orderDb, book);
        return orderRepository.save(orderDb);
    }

    // cancel order
    @PostMapping("/orders/cancel")
    public Order cancelOrder(@RequestBody Order order) {
        Order orderDb = orderRepository.findByEmailAndIsbn(order.getEmail(), order.getIsbn());
        if (null == orderDb) {
            throw new BadRequestException("Order not found, ISBN " + order.getIsbn() + " client " + order.getEmail());
        }
        if (!orderDb.isCompleted()) {
            throw new PurchaseForbiddenException("Order is not paid, ISBN " + order.getIsbn() + " client " + order.getEmail());
        }
        verifyClient(order.getEmail());
        Storage storage = verifyStorage(order.getIsbn(), order.getQuantity());
        orderDb.setQuantity(order.getQuantity());

        returnToStorage(storage, orderDb);
        return orderRepository.save(orderDb);
    }

    // delete an order
    @DeleteMapping("/orders/{id}")
    public void deleteOrderById(@PathVariable Long id) {
        orderRepository.deleteById(id);
    }

    // delete all orders
    @DeleteMapping("/orders/delete-all")
    public void deleteAllBooks() {
        orderRepository.deleteAll();
    }

    private void verifyClient(String email) {
        Client client = clientRepository.getClientByEmail(email);
        if (null == client) {
            throw new ResourceNotFoundException("Client is not found by email " + email);
        }
        Client[] clients = clientRepository.getAllClients();
        logger.debug(clients.toString());
    }

    private Book verifyBook(String isbn) {
        Book book = bookRepository.getBookByISBN(isbn);
        if (null == book) {
            throw new ResourceNotFoundException("Book not found by isbn " + isbn);
        }
        if (!book.isPublished()) {
            throw new ResourceNotFoundException("The book is not yet vendible, ISBN: " + isbn);
        }
        Book[] books = bookRepository.getAllBooks();
        logger.debug(books.toString());
        return book;
    }

    private Storage verifyStorage(String isbn, int quantity) {
        Storage storage = storageRepository.getStorageByISBN(isbn);
        if (null == storage || storage.getQuantity() < quantity) {
            throw new InsufficientResourcesException("We do not have enough books in storage, ISBN: " + isbn);
        }
        Storage[] storages = storageRepository.getAllBooksInStorage();
        logger.debug(storages.toString());
        return storage;
    }

    private void buyFromStorage(Storage storage, Order order, Book book) {
        if (!storage.getIsbn().equals(order.getIsbn())) {
            throw new BadRequestException("Wrong storage for ISBN: " + order.getIsbn());
        }
        storage.setQuantity(order.getQuantity());
        if (!order.isCompleted()) {
            order.setCompleted(true);
        }
        if (book.getPrice() > order.getPrice()) {
            throw new PurchaseForbiddenException("Price changed for book ISBN: " + book.getIsbn());
        } else if (book.getPrice() < order.getPrice()) {
            order.setPrice(book.getPrice());
        }
        try {
            storageRepository.buyBook(storage);
            payOrder(order);
        } catch (PurchaseForbiddenException purchaseForbiddenException) {
            order.setCompleted(false);
        } catch (PaymentException ignored) {
            order.setCompleted(false);
        }
    }

    private void returnToStorage(Storage storage, Order order) {
        if (!storage.getIsbn().equals(order.getIsbn())) {
            throw new BadRequestException("Wrong storage for ISBN: " + order.getIsbn());
        }
        if (order.isCompleted()) {
            order.setCompleted(false);
        }
        storage.setQuantity(order.getQuantity());
        try {
            storageRepository.returnBook(storage);
        } catch (PurchaseForbiddenException purchaseForbiddenException) {
            order.setCompleted(true);
        }
    }

    private void payOrder(Order order) {
        Payment payment = new Payment(order.getId(), order.getPrice() * order.getPrice(), order.getEmail());
        payment = paymentRepository.submitPayment(payment);
        if (null == payment || !payment.isSucceeded()) {
            throw new PaymentException(null == payment ? "Payment Failed" : payment.getMessage());
        }
    }

    private void simulateHardWork() {
        int arraySize = (int)((long)this.memPressureMb * 1024L * 1024L / 8L);
        if (arraySize < 0) {
            arraySize = Integer.MAX_VALUE;
        }
        long[] longs = new long[arraySize];
        int j = 0;
        for(long i = 0; i < this.cpuPressure; i++, j++) {
            j++;
            if (j >= arraySize) {
                j = 0;
            }
            try {
                if (longs[j] > Integer.MAX_VALUE) {
                    longs[j] = (long) Integer.MIN_VALUE;
                }
            } catch (Exception ignored) {};
        }
    }
}
