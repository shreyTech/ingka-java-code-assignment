package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@ApplicationScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject
    private WarehouseStore warehouseRepository;

    @Inject
    private LocationResolver locationResolver;

    @Inject
    private CreateWarehouseOperation createWarehouseUse;

    @Inject
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.findAllWareHouses();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        Location location = validateLocation(data);
        List<Warehouse> warehouses = warehouseRepository.findByLocation(location.identification);
        verifyCapacityAtLocation(data, warehouses, location.maxCapacity, location.maxNumberOfWarehouses);
        createWarehouseUse.create(data);
        return data;
    }

    private void verifyCapacityAtLocation(Warehouse newWarehouse, List<Warehouse> warehouses, int maxCapacity,
                                                 int maxNumberOfWarehouses) {
        Integer usedCapacity = 0;
        Integer usedStock = 0;
        if (warehouses != null) {
            usedCapacity = warehouses.stream().map((w -> w.capacity)).reduce(Integer::sum).orElse(0);
            usedStock = warehouses.stream().map((w -> w.stock)).reduce(Integer::sum).orElse(0);
        }
        verifyWarehouseCapacity(newWarehouse, maxCapacity, maxNumberOfWarehouses, usedCapacity, usedStock);
    }

    private void verifyWarehouseCapacity(Warehouse newWarehouse, int maxCapacity, int maxNumberOfWarehouses,
                                         Integer usedCapacity, Integer usedStock) {
        if (usedCapacity + newWarehouse.capacity > maxCapacity) {
            throw new WebApplicationException("maxCapacity reached at location", 422);
        }
        if (usedStock + newWarehouse.stock > maxNumberOfWarehouses) {
            throw new WebApplicationException("maxNumberOfWarehouses reached at location", 422);
        }
    }

    private Location validateLocation(Warehouse data) {
        Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(data.businessUnitCode);
        if (warehouse != null) {
            throw new WebApplicationException(String.format("The business Unit of the warehouse already exists %s",
                    data.businessUnitCode), 404);
        }
        return locationResolver.resolveByIdentifier(data.location);
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        return warehouseRepository.findById(id);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {
        Warehouse warehouse = warehouseRepository.findById(id);
        if (warehouse == null) {
            throw new WebApplicationException(String.format("No warehouse found for id %s", id), 404);
        }
        archiveWarehouseOperation.archive(warehouse);
    }

    @Override
    public Warehouse replaceWarehouseUnit(String id, @NotNull Warehouse newWarehouse) {
        Warehouse existingWareHouse = getAWarehouseUnitByID(id);
        if (existingWareHouse == null) {
            throw new WebApplicationException(String.format("No Warehouse found for id %s", id), 404);
        }
        Location location = validateLocation(existingWareHouse);

        List<Warehouse> warehouses = warehouseRepository.findByLocation(existingWareHouse.location);
        //Calculate total capacity used
        Integer usedCapacity = warehouses.stream().map((w -> w.capacity)).reduce(Integer::sum).orElse(0);
        Integer usedStock = warehouses.stream().map((w -> w.stock)).reduce(Integer::sum).orElse(0);
        //Remove the capacity of warehouse which will be replaced by newWarehouse
        usedCapacity = usedCapacity - existingWareHouse.capacity;
        usedStock = usedStock - existingWareHouse.stock;

        verifyWarehouseCapacity(newWarehouse, location.maxCapacity, location.maxNumberOfWarehouses,
            usedCapacity, usedStock);

        replaceWarehouse(newWarehouse, existingWareHouse);
        return newWarehouse;
    }

    @Transactional
    private void replaceWarehouse(Warehouse newWarehouse, Warehouse existingWareHouse) {

        //Archive existing WareHouse
        archiveWarehouseOperation.archive(existingWareHouse);

        //Create new WareHouse
        createWarehouseUse.create(newWarehouse);
    }

}
