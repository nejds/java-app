package com.example.app.model;

import java.sql.Timestamp;

public record Transaction(long id, long userId, int amount, boolean income, Timestamp createdAt) {}
