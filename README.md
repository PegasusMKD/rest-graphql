# RQL
### This is a library meant to solve issues regarding *Spring Data JPA* & *general database issues* such as:
 - n+1 issues
 - Cartesian products

### Beside solving the mentioned issues, it also brings:
 - GraphQL-like features on a REST interface

## Inner workings
 This is achieved through pre-processing of the entities using reflection to check which
 properties will be fetched eagerly (due to bad configurations of annotations), then
 dynamically creating an entity graph to fetch those properties eagerly, instead of having the n+1 issues.
 
 In the pre-processing step, we create a tree of all the properties/relations. If one of the nodes is
 a one-to-many relation, it gets "separated" as a sub-graph, and we do a special select for that sub-graph with which
 we avoid the cartesian products. Then, the returned data gets mapped back to the parent at runtime.

 Both the pre-processing & "separation" are done recursively, so the depth to which it can go isn't restricted. 

## Mainly useful for
 If you are need of:
 - An implementation of GraphQL which wouldn't require many changes to existing/legacy code
 - Returning multiple collections with relatively high speed and performance
 - Minimizing the quantity/number of queries being sent to a database

## Caveats
 A couple of current restrictions/caveats regarding the code-base:
 - Too opinionated **(for my liking)**
 - Bad Many-To-Many support
   - Currently, you can call for only 1 Many-To-Many collection in a query
   - You can't request for collections within a Many-To-Many collection (it won't be processed efficiently)
 - Data **HAS** to be passed as a List (so the developer is expected to cast/recast)
 - If the project is using Lombok, all collections have to be ***excluded*** from the `@EqualsAndHashCode` annotation
 - Naming conventions
   - Names of properties/collections have to be formatted in the following format "|className|s",
     for example a collection of type `Post` has to be named `posts`
 - Both sides need to mark the relation (for example `ManyToOne` relation needs to be marked
   on the parent side as well with a `OneToMany` annotation)     

## Planned improvements
 Things which are currently being worked on:
 - Implementing partitioning & threading of data while mapping it out
   - For example, if we have 1 million records which we'd need to map out to their corresponding parents,
     we'd partition them in collections of 50000, open threads for each collection & then start mapping them
     out simultaneously
 - Properly packaging the code-base
 - Automatically generating required mappers, repositories & processing units using Google's auto-processor
 - Adding customizations
    - Restricting which properties are allowed to be fetched using
      the GraphQL functionalities
 - Proper documentation
 - Proper Many-To-Many support

## Performance
 - Cartesian Product
   - From disabling the cartesian product a query which takes ~20s execution time gets
     lowered to ~1s execution time
       - The query fetches 1 parent with 2 properties (which are collections) 
         or in total 40200 records (each collection totals 20000 records)