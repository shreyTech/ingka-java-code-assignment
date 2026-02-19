package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WarehouseResourceImplTest {

    @Mock
    private WarehouseStore warehouseRepository;

    @Mock
    private LocationResolver locationResolver;

    @Mock
    private CreateWarehouseOperation createWarehouseUse;

    @Mock
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @InjectMocks
    private WarehouseResource warehouseResource = new WarehouseResourceImpl();

    List<DbWarehouse> getDbWarehouses() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.location = "Edinburgh";
        dbWarehouse.stock = 10;
        dbWarehouse.capacity = 2;
        dbWarehouse.businessUnitCode = "Printer";
        dbWarehouse.id = 1L;

        DbWarehouse anotherDbWarehouse = new DbWarehouse();
        anotherDbWarehouse.location = "Glasgow";
        anotherDbWarehouse.stock = 20;
        anotherDbWarehouse.capacity = 1;
        anotherDbWarehouse.businessUnitCode = "Camera";
        anotherDbWarehouse.id = 2L;

        return List.of(dbWarehouse, anotherDbWarehouse);
    }

    List<Warehouse> getWarehouses() {
        Warehouse warehouse = new Warehouse();
        warehouse.location = "Edinburgh";
        warehouse.stock = 10;
        warehouse.capacity = 2;
        warehouse.businessUnitCode = "Printer";

        Warehouse anotherWarehouse = new Warehouse();
        anotherWarehouse.location = "Glasgow";
        anotherWarehouse.stock = 20;
        anotherWarehouse.capacity = 1;
        anotherWarehouse.businessUnitCode = "Camera";

        return List.of(warehouse, anotherWarehouse);
    }

    @Test
    void listAllWarehousesUnits() {

        Mockito.when(warehouseRepository.findAllWareHouses()).thenReturn(getWarehouses());

        // when
        List<Warehouse> warehouses = warehouseResource.listAllWarehousesUnits();

        // then
        assertEquals(warehouses.size(), 2);
    }

    @Test
    void createANewWarehouseUnit() {

        //Given a warehouse unit
        Warehouse warehouse = getWarehouses().get(0);
        Mockito.when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(new Location(warehouse.location, 10, 40));
        Mockito.when(warehouseRepository.findByLocation(warehouse.location)).thenReturn(null);

        // when a request is made to create warehouse
        Warehouse created = warehouseResource.createANewWarehouseUnit(warehouse);

        // then the warehouse is created successfully
        assertEquals(created.businessUnitCode, warehouse.businessUnitCode);
        assertEquals(created.location, warehouse.location);
        assertEquals(created.stock, warehouse.stock);
        assertEquals(created.capacity, warehouse.capacity);
        Mockito.verify(createWarehouseUse).create(warehouse);
    }

    @Test
    void createANewWarehouseUnitThatExceedsMaxNumberOfWarehousesLimit() {

        //Given a warehouse unit, adding which will breach the maxNumberOfWareHouse limit
        Warehouse warehouse = getWarehouses().get(0);
        Mockito.when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(new Location(warehouse.location, 2, 40));
        Mockito.when(warehouseRepository.findByLocation(warehouse.location)).thenReturn(null);

        // when a request is made to create warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> warehouseResource.createANewWarehouseUnit(warehouse));

        // then an exception is thrown
        assertEquals("maxNumberOfWarehouses reached at location", exception.getMessage());

    }

    @Test
    void createANewWarehouseUnitThatExceedsMaxCapacityLimit() {

        //Given a warehouse unit, adding which will breach the maxCapacity limit
        Warehouse warehouse = getWarehouses().get(0);
        Mockito.when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(new Location(warehouse.location, 10, 1));
        Mockito.when(warehouseRepository.findByLocation(warehouse.location)).thenReturn(null);

        // when a request is made to create warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> warehouseResource.createANewWarehouseUnit(warehouse));

        // then an exception is thrown
        assertEquals("maxCapacity reached at location", exception.getMessage());

    }
    @Test
    void getAWarehouseUnitByID() {

        //Given a warehouse unit
        Warehouse warehouse = getWarehouses().get(0);
        Mockito.when(warehouseRepository.findById(warehouse.businessUnitCode)).thenReturn(warehouse);

        // when
        Warehouse created = warehouseResource.getAWarehouseUnitByID(warehouse.businessUnitCode);

        // then the warehouse is created successfully
        assertEquals(created.businessUnitCode, warehouse.businessUnitCode);
        assertEquals(created.location, warehouse.location);
        assertEquals(created.stock, warehouse.stock);
        assertEquals(created.capacity, warehouse.capacity);
    }

    @Test
    void archiveAWarehouseUnitByID() {

        //Given a warehouse unit
        Warehouse warehouse = getWarehouses().get(0);
        String id = "1";
        Mockito.when(warehouseRepository.findById(id)).thenReturn(warehouse);

        // when a request is made to create warehouse
        warehouseResource.archiveAWarehouseUnitByID("1");

        // then the warehouse is archived successfully
        Mockito.verify(archiveWarehouseOperation).archive(warehouse);
    }

    @Test
    void replaceWarehouseUnit() {

        //Given a warehouse unit
        Warehouse existingWarehouse = getWarehouses().get(0);
        Warehouse newWarehouse = getWarehouses().get(1);
        newWarehouse.location = existingWarehouse.location;

        String id = "1";
        Mockito.when(warehouseRepository.findById(id)).thenReturn(existingWarehouse);
        Mockito.when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(new Location(newWarehouse.location, 50, 40));
        Mockito.when(warehouseRepository.findByLocation(newWarehouse.location)).thenReturn(List.of(existingWarehouse, newWarehouse));

        // when a request is made to create warehouse
        warehouseResource.replaceWarehouseUnit("1", newWarehouse);

        // then the existing warehouse is archived and new one is created successfully
        Mockito.verify(createWarehouseUse).create(newWarehouse);
        Mockito.verify(archiveWarehouseOperation).archive(existingWarehouse);
    }

    @Test
    void replaceWarehouseUnitThatExceedsMaxCapacityLimit() {

        //Given a warehouse unit adding which will breach maxCapacityLimit
        Warehouse existingWarehouse = getWarehouses().get(0);
        Warehouse newWarehouse = getWarehouses().get(1);
        newWarehouse.location = existingWarehouse.location;

        String id = "1";
        Mockito.when(warehouseRepository.findById(id)).thenReturn(existingWarehouse);
        Mockito.when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(new Location(newWarehouse.location, 10, 40));
        Mockito.when(warehouseRepository.findByLocation(newWarehouse.location)).thenReturn(List.of(existingWarehouse, newWarehouse));

        // when a request is made to replace the warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->warehouseResource.replaceWarehouseUnit("1", newWarehouse));

        // then an exception is thrown
        assertEquals("maxNumberOfWarehouses reached at location", exception.getMessage());

    }

}