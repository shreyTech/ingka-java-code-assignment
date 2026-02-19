package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  DbWarehouse transform(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    return dbWarehouse;
  }

  Warehouse transform(DbWarehouse dbWarehouse) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = dbWarehouse.businessUnitCode;
    warehouse.location = dbWarehouse.location;
    warehouse.capacity = dbWarehouse.capacity;
    warehouse.stock = dbWarehouse.stock;
    return warehouse;
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = transform(warehouse);
    dbWarehouse.createdAt = LocalDateTime.now();
    persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    this.persist(transform(warehouse));
  }


  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse dbWarehouse = transform(warehouse);
    dbWarehouse.archivedAt = LocalDateTime.now();
    this.persist(dbWarehouse);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    Optional<DbWarehouse> dbWarehouse = find("businessUnitCode", buCode).stream().findFirst();
    return dbWarehouse.map(this::transform).orElse(null);
  }

  @Override
  public List<Warehouse> findByLocation(String locationId) {
    List<DbWarehouse> dbWarehouse = list("location=? and archivedAt is null", locationId);
    return dbWarehouse.stream().map(this::transform).toList();
  }

  @Override
  public Warehouse findById(String id) {
    Optional<DbWarehouse> dbWarehouse = find("id", id).stream().findFirst();
    return dbWarehouse.map(this::transform).orElse(null);
  }

  @Override
  public List<Warehouse> findAllWareHouses() {
    return find("archivedAt is null").stream().map(this::transform).toList();
  }

}