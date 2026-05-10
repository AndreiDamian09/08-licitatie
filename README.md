# 08 - Licitatie

Aplicatie distribuita client-server pentru organizarea de licitatii, implementata in Java prin TCP sockets.

## Functionalitati

- conectare clienti cu nume unic
- trimitere lista produse la conectare
- publicare produs nou (nume unic, pret minim)
- ofertare valida doar cu suma mai mare decat pretul curent
- afisare ofertant castigator prin campul `highestBidder`
- listarea produselor castigate de client prin comanda `WINS`
- blocarea licitarii de catre proprietarul produsului
- notificari la publicare, ofertare si expirare
- expirare automata configurabila a licitatiilor
- tratare erori (nume duplicat, produs inexistent/expirat, oferta invalida)

## Structura proiect

```text
08-licitatie/
|-- README.md
|-- docker-compose.yml
|-- server/
|   |-- Dockerfile
|   `-- src/
|       |-- MainServer.java
|       |-- AuctionServer.java
|       |-- AuctionItem.java
|       |-- AuctionEvent.java
|       |-- AuctionEventPublisher.java
|       |-- AuctionObserver.java
|       `-- ClientConnection.java
`-- client/
    |-- Dockerfile
    `-- src/
        `-- MainClient.java
```

## Design pattern folosit: Observer

Serverul foloseste pattern-ul GoF Observer pentru notificarea clientilor:

- `AuctionEventPublisher` este Subject-ul care pastreaza observatorii conectati.
- `AuctionObserver` defineste interfata observatorilor.
- `ClientConnection` implementeaza `AuctionObserver` si primeste evenimente prin metoda `update`.
- `AuctionServer` publica evenimente precum `CLIENT_JOINED`, `PRODUCT_PUBLISHED`, `BID_UPDATE`, `BID_NOTICE` si `AUCTION_EXPIRED`.

Astfel, logica de licitatie nu mai trimite direct mesaje catre fiecare client, ci emite evenimente catre publisher.

## Rulare cu Podman

Nu este necesar sa ai Java instalat local. Imaginea `eclipse-temurin:21-jdk` din Dockerfile contine JDK-ul, iar Podman compileaza si ruleaza codul in container.

Din radacina proiectului `08-licitatie`:

```bash
podman compose build
podman compose up auction-server
```

Serverul asculta implicit pe `localhost:5000`.

Pentru un client interactiv, deschide alt terminal si ruleaza:

```bash
podman compose run --rm auction-client
```

Pentru scenariul de licitatie, deschide 2-3 terminale separate si ruleaza aceeasi comanda de client in fiecare.

Daca instalarea ta foloseste utilitarul separat `podman-compose`, comenzile echivalente sunt:

```bash
podman-compose build
podman-compose up auction-server
podman-compose run --rm auction-client
```

Variabile configurabile:

- `SERVER_PORT` (default 5000)
- `AUCTION_DURATION_SECONDS` (default 30)

## Build manual cu Podman

Daca vrei sa rulezi fara Compose:

```bash
podman build -t auction-server ./server
podman run --rm --name auction-server -p 5000:5000 auction-server
```

Intr-un alt terminal:

```bash
podman build -t auction-client ./client
podman run --rm -it --network host auction-client localhost 5000
```

## Comenzi client

- `CONNECT <nume>`
- `LIST`
- `WINS`
- `PUBLISH <nume_produs> <pret_minim>`
- `BID <nume_produs> <suma>`
- `QUIT`

In raspunsurile serverului, `owner` este proprietarul/vanzatorul produsului, iar `highestBidder` este clientul cu cea mai mare oferta. La finalul licitatiei, produsul ramane cu acelasi `owner`, dar castigatorul este `highestBidder`.

Comanda `WINS` afiseaza doar produsele expirate castigate de clientul conectat.

## Scenariu demo rapid

Client 1:

```text
CONNECT Andrei
PUBLISH Telefon 500
```

Client 2:

```text
CONNECT Maria
BID Telefon 600
```

Client 3:

```text
CONNECT Alex
BID Telefon 750
```

Dupa expirare:

```text
BID Telefon 900
```

Serverul va refuza oferta daca produsul este expirat.

## Oprire

```bash
podman compose down
```
