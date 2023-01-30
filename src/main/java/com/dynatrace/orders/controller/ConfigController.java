package com.dynatrace.orders.controller;

import com.dynatrace.orders.exception.ResourceNotFoundException;
import com.dynatrace.orders.model.Config;
import com.dynatrace.orders.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {
    @Autowired
    private ConfigRepository configRepository;

    // get all settings
    @GetMapping("")
    public List<Config> getAllConfigs() {
        return configRepository.findAll();
    }

    // get a setting
    @GetMapping("/{id}")
    public Config getConfigById(@PathVariable String id) {
        Optional<Config> config = configRepository.findById(id);
        if (config.isEmpty()) {
            throw new ResourceNotFoundException("Config does not exist");
        }
        return config.get();
    }

    // create a setting
    @PostMapping("")
    public Config createConfig(@RequestBody Config config) {
        return configRepository.save(config);
    }

    // update a config
    @PutMapping("/{id}")
    public Config updateConfig(@PathVariable String id, @RequestBody Config config) {
        Optional<Config> configDb = configRepository.findById(id);
        if (configDb.isEmpty()) {
            throw new ResourceNotFoundException("Config does not exist");
        }
        return configRepository.save(config);
    }

    // delete a config
    @DeleteMapping("/{id}")
    public void deleteConfig(@PathVariable String id) {
        configRepository.deleteById(id);
    }
}
