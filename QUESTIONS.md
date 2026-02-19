# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
I can see different implementation strategies being used in com.fulfilment.application.monolith.stores
and com.fulfilment.application.monolith.warehouses.adapters.restapi, I think controller should not have a direct dependency on persistent/repository layer, instead the controller should delegate the call to service which in turn should update the repository.
```txt

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
The pros of generating code from the spec is that the code is guaranteed to be consistent with the schema and it’s faster to have a simple implementation. The cons however is that the dependency between different layers in the code is not aligned when the code is  generated (for instance the StoreResource is directly dependent on Store repository and uses Store Entity directly), also custom handling of different errors needs to be completed additionally.
```txt

```
