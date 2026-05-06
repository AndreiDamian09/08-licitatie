# 08 - Licitatie

Aplicatie distribuita client-server pentru organizarea de licitatii, implementata in Java prin TCP sockets.

## Functionalitati

- conectare clienti cu nume unic
- trimitere lista produse la conectare
- publicare produs nou (nume unic, pret minim)
- ofertare valida doar cu suma mai mare decat pretul curent
- notificari la publicare, ofertare si expirare
- expirare automata configurabila a licitatiilor
- tratare erori (nume duplicat, produs inexistent/expirat, oferta invalida)

## Structura proiect

```text
08-licitatie/
├── README.md
├── docker-compose.yml
├── server/
│   ├── Dockerfile
│   └── src/
│       ├── MainServer.java
│       ├── AuctionServer.java
│       ├── AuctionItem.java
│       └── ClientConnection.java
└── client/
    ├── Dockerfile
    └── src/
        └── MainClient.java
```

## Rulare server in Docker

Din radacina proiectului:

```bash
docker compose up --build
```

Serverul asculta implicit pe `localhost:5000`.

Variabile configurabile:

- `SERVER_PORT` (default 5000)
- `AUCTION_DURATION_SECONDS` (default 30)

## Rulare clienti local

Intr-un terminal:

```bash
cd client
javac src/*.java
java -cp src MainClient
```

Deschide cel putin 3 terminale pentru a demonstra scenariile din cerinta.

## Comenzi client

- `CONNECT <nume>`
- `LIST`
- `PUBLISH <nume_produs> <pret_minim>`
- `BID <nume_produs> <suma>`
- `QUIT`

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
docker compose down
```
