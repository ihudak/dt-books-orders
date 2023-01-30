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
@RequestMapping("/api/v1/orders")
public class OrderController extends HardworkingController {
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
    @Autowired
    ConfigRepository configRepository;
    Logger logger = LoggerFactory.getLogger(OrderController.class);


    // get all Orders
    @GetMapping("")
    public List<Order> getAllCarts() {
        return orderRepository.findAll(Sort.by(Sort.Direction.ASC, "email", "createdAt"));
    }

    // get Order by ID
    @GetMapping("/{id}")
    public Order getCartById(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order.get();
    }

    // get Orders of a user
    @GetMapping("/findByEmail")
    public List<Order> getCartsByEmail(@RequestParam String email) {
        this.verifyClient(email);
        return orderRepository.findByEmail(email);
    }

    // get all users who ordered the book
    @GetMapping("/findByISBN")
    public List<Order> getCartsByISBN(@RequestParam String isbn) {
        this.verifyBook(isbn);
        return orderRepository.findByEmail(isbn);
    }

    // create an order
    @PostMapping("")
    public Order createOrder(@RequestBody Order order) {
        simulateHardWork();
        simulateCrash();
        Book book = verifyBook(order.getIsbn());
        order.setPrice(book.getPrice()); // new order - taking the fresh price
        verifyClient(order.getEmail());
        Storage storage = verifyStorage(order.getIsbn(), order.getQuantity());
        if (order.isCompleted()) {
            buyFromStorage(storage, order, book);
        }
        logger.debug("Created order for book " + order.getIsbn() + " client " + order.getEmail());
        return orderRepository.save(order);
    }

    // update an order
    @PutMapping("/{id}")
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
    @PostMapping("/submit")
    public Order submitOrder(@RequestBody Order order) {
        simulateHardWork();
        simulateCrash();
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
        logger.debug("Submitted order for book " + order.getIsbn() + " client " + order.getEmail());
        return orderRepository.save(orderDb);
    }

    // cancel order
    @PostMapping("/cancel")
    public Order cancelOrder(@RequestBody Order order) {
        simulateHardWork();
        simulateCrash();
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
        logger.debug("Canceled order for book " + order.getIsbn() + " client " + order.getEmail());
        return orderRepository.save(orderDb);
    }

    // delete an order
    @DeleteMapping("/{id}")
    public void deleteOrderById(@PathVariable Long id) {
        orderRepository.deleteById(id);
    }

    // delete all orders
    @DeleteMapping("/delete-all")
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
        simulateHardWork();
        simulateCrash();
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
        } catch (PurchaseForbiddenException purchaseForbiddenException) {
            order.setCompleted(false);
            throw purchaseForbiddenException;
        }
        try {
            payOrder(order);
        } catch (PaymentException paymentException) {
            storageRepository.returnBook(storage);
            order.setCompleted(false);
            throw paymentException;
        }
        logger.debug("Took from Storage book " + order.getIsbn() + " client " + order.getEmail());
    }

    private void returnToStorage(Storage storage, Order order) {
        simulateHardWork();
        simulateCrash();
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
        logger.debug("Returned order for book " + order.getIsbn() + " client " + order.getEmail());
    }

    private void payOrder(Order order) {
        simulateHardWork();
        simulateCrash();
        Payment payment = new Payment(order.getId(), order.getPrice() * order.getPrice(), order.getEmail());
        payment = paymentRepository.submitPayment(payment);
        if (null == payment || !payment.isSucceeded()) {
            throw new PaymentException(null == payment ? "Payment Failed" : payment.getMessage());
        }
        logger.debug("Paid order for book " + order.getIsbn() + " client " + order.getEmail());
    }

    @Override
    public ConfigRepository getConfigRepository() {
        return configRepository;
    }
}
