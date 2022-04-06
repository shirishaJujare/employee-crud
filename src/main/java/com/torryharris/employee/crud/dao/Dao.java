package com.torryharris.employee.crud.dao;

import io.vertx.core.Promise;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

  Promise<Optional<T>> get(long id);

  Promise<List<T>> getAll();

  Promise<T> save(T t);

  Promise<Integer> update(T t);

  Promise<Integer> delete(long id);
}
