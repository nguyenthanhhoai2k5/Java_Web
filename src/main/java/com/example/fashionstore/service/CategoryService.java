package com.example.fashionstore.service;

import com.example.fashionstore.model.Category;
import com.example.fashionstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository repository;

    public List<Category> getAll() { return repository.findAll(); }

    public void save(Category category) { repository.save(category); }

    public void delete(Long id) { repository.deleteById(id); }

    public Category getById(Long id) { return repository.findById(id).orElse(null); }
}