# 08 - Licitatie

Aplicatie distribuita de tip **client-server** pentru organizarea de licitatii, realizata in **Java (socket-uri TCP)**.

Proiect realizat pentru materia **Retele de Calculatoare**.

---

# Functionalitati implementate

## Conectare clienti

- clientii se conecteaza la server folosind un nume unic
- daca numele exista deja, conexiunea este refuzata
- la conectare se trimite lista produselor active

## Publicare produs

Un client poate adauga un produs nou:

- nume produs unic
- pret minim de pornire

Serverul:

- adauga produsul
- seteaza pretul curent
- notifica toti clientii conectati

## Licitare

Clientii pot face oferte pentru produse active.

Oferta este valida daca:

- produsul exista
- produsul este activ
- suma este mai mare decat pretul curent

Daca oferta este valida:

- se actualizeaza pretul maxim
- se notifica proprietarul
- se notifica participantii anteriori

## Expirare automata

Fiecare licitatie are o durata configurabila.

La expirare:

- produsul devine indisponibil
- ofertele noi sunt refuzate
- toti clientii sunt notificati

## Tratarea erorilor

- nume duplicat
- produs duplicat
- oferta prea mica
- produs expirat
- deconectare client fara crash

---

# Tehnologii folosite

- Java
- TCP Sockets
- Multithreading
- Docker
- Docker Compose

---

# Structura proiectului

```txt
08-licitatie/
│
├── README.md
├── docker-compose.yml
│
├── server/
│   ├── Dockerfile
│   └── src/
│
└── client/
    ├── Dockerfile
    └── src/
```

---

# Cerinte preliminare

Trebuie instalate:

- Java JDK 17+ sau 21+
- Docker Desktop
- Git (optional)

Verificare:

```bash
java -version
docker -v
docker compose version
```

---

# Rulare proiect

# Varianta recomandata (Server in Docker + Client local)

## 1. Porneste serverul

Din radacina proiectului:

```bash
docker compose up --build
```

Serverul va porni pe:

```txt
localhost:5000
```

---

## 2. Ruleaza clientul local din terminal

Mergi in folderul client:

```bash
cd client/src
```

Compileaza:

```bash
javac *.java
```

Ruleaza:

```bash
java MainClient
```

Deschide 3 terminale pentru 3 clienti.

---

# Varianta Full Docker

## Pornire server

```bash
docker compose up --build
```

## Build client image

In alt terminal:

```bash
docker build -t licitatie-client ./client
```

## Ruleaza clienti

```bash
docker run -it --network 08-licitatie_default licitatie-client
docker run -it --network 08-licitatie_default licitatie-client
docker run -it --network 08-licitatie_default licitatie-client
```

---

# Oprire proiect

```bash
docker compose down
```

---

# Comenzi disponibile in client

## Conectare

```txt
CONNECT Andrei
```

## Vezi produse

```txt
LIST
```

## Publica produs

```txt
PUBLISH Telefon 500
```

## Plaseaza oferta

```txt
BID Telefon 700
```

## Iesire

```txt
QUIT
```

---

# Exemplu demo rapid

Terminal 1:

```txt
CONNECT Andrei
PUBLISH Telefon 500
```

Terminal 2:

```txt
CONNECT Maria
BID Telefon 600
```

Terminal 3:

```txt
CONNECT Alex
BID Telefon 750
```

Dupa expirare:

```txt
BID Telefon 900
=> Oferta refuzata
```

---

# Probleme frecvente

## Port ocupat

Schimba portul in `docker-compose.yml`

## Docker nu porneste

Porneste Docker Desktop.

## Clientul nu se conecteaza

Verifica daca serverul ruleaza.

## Class not found

Ruleaza comenzile din folderul corect.

---

# Video demonstrativ

Adauga link:

```txt
https://youtube.com/...
```

---

# Autor

Andrei Damian, Chisega Eduard, Buzila Cosmin
