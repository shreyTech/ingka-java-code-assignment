package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface WarehouseResource {

    List<Warehouse> listAllWarehousesUnits();

    Warehouse createANewWarehouseUnit(@NotNull Warehouse data);

    Warehouse getAWarehouseUnitByID(String id);

    void archiveAWarehouseUnitByID(String id);

    Warehouse replaceWarehouseUnit(String id, @NotNull Warehouse data);
}
