# Documentation

## Design

### First Steps

The main Project Structure was of the httpserverbase Repo.\
It was then simplified to just use Services to map to more Services.\
Which in most Cases were just Controller Methods with the same Signature.\
Also Instead of Repositories just simple DB Access Classes where used.\
These just have Methods to only run DB Queries which get used in Controller Methods.\
The Postgres Database was run via Docker.\
with an SQL script being executed when building the Container with the DockerFile.

### Database Design

The main Tables were made (users, cards, deck, stack, packages).\
Later on deck/stack/package_cards tables were made to hold the card Ids of those Tables.\
For Package purchaes the transactions table got referenced by the package table to determine if a Package has been bought.\
Lastly I made a rading_deals table which hold all the reading deals.\
This made me need to change the stack_cards table to show if a card is in deck or in a deal.


### Structure

- `src/main/java` : Has the main SourceCode
- `src/test/java` : Has the main Testing SourceCode
  - `src/main/java/org/mtcg` : Main Package
      - `Main` : The Main class which starts the Server and configures the Router
      - `httpserver` : Holds most of the Server Side Code
      - `utils` : Has utils like Enums the Router and misc Battle Utilities
      - `models` : Contains the Model Classes like user and Card
      - `controllers` : This folder contains all the controller Classes which hold the main Business Logic for whole app
      - `db` : In here is the Database Connection Class and various Access Classes which execute Database Queries

[Specification](./MTCG_Specification.pdf)

[Here](./mtcg-api.yaml) you can find the API Endpoints which are implemented almost each with a new Service which maps the Methods to Controller Methods which themselves are there own Services\


