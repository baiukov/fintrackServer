# FintrackServer

FintrackServer je backendová část aplikace pro sledování financí. Tento projekt je napsán v Javě a využívá moderní technologie jako Spring Boot pro vývoj REST API. Server poskytuje funkce pro správu finančních dat, jako jsou transakce, účty a kategorie.

## Funkce projektu
- CRUD operace pro účty, transakce a kategorie
- Autentizace a autorizace pomocí JWT
- Integrace s databází prostřednictvím JPA/Hibernate
- Výkonné dotazy a agregace dat

## Požadavky
Před spuštěním projektu se ujistěte, že máte nainstalovány následující nástroje:
- [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (doporučená verze: 11 nebo vyšší)
- [Maven](https://maven.apache.org/) pro správu závislostí
- Aktivní databázový server (např. PostgreSQL nebo MySQL)
- [Git](https://git-scm.com/) pro správu verzí

## Instalace a spuštění

### 1. Klonování repozitáře
Naklonujte tento repozitář do svého počítače:
`bash
git clone https://github.com/baiukov/fintrackServer.git
cd fintrackServer `

### 2. Konfigurace prostředí
Tento projekt používá soubor .env nebo application.properties pro nastavení prostředí. Ujistěte se, že jste nastavili následující parametry:

Ukázka application.properties:

spring.datasource.url=jdbc:oracle://localhost:1521/fintrack
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

jwt.secret=your_jwt_secret
jwt.expiration=86400

Pokud používáte jiný typ databáze (např. MySQL), upravte spring.datasource.url a hibernate.dialect podle dokumentace Hibernate.

### 3. Inicializace databáze
Spusťte váš databázový server a vytvořte databázi:
`CREATE DATABASE fintrack`;
Ujistěte se, že zadané přihlašovací údaje v application.properties odpovídají vašemu databázovému serveru.

### 4. Sestavení a spuštění aplikace
Použijte Maven k sestavení a spuštění aplikace:

mvn clean install
mvn spring-boot:run

Po úspěšném spuštění bude server naslouchat na http://localhost:8080. 

### Testování API
Pro testování API můžete použít nástroje jako Postman nebo Swagger. Zde je příklad dotazu na API:


### Další poznámky k instalaci
Bezpečnostní úložiště: Server používá JWT pro autentizaci. Ujistěte se, že jste nastavili jwt.secret ve vašem application.properties.
Interceptors: Server obsahuje interceptory pro zpracování bezpečnostních požadavků. Zkontrolujte, že vaše aplikace odpovídá požadavkům zabezpečení.
Struktura projektu
src/main/java: Obsahuje zdrojový kód aplikace, včetně konfigurací, kontrolerů, služeb a repozitářů.
src/main/resources: Obsahuje konfigurační soubory, jako je application.properties.
src/test/java: Obsahuje testovací třídy pro ověření funkcionality aplikace.

### Příspěvky
Pokud chcete přispět do tohoto projektu, neváhejte vytvořit pull request nebo nahlásit chybu v sekci Issues.

### Licence
Tento projekt je licencován pod licencí MIT.
# FintrackServer

FintrackServer je backendová část aplikace pro sledování financí. Tento projekt je napsán v Javě a využívá moderní technologie jako Spring Boot pro vývoj REST API. Server poskytuje funkce pro správu finančních dat, jako jsou transakce, účty a kategorie.

## Funkce projektu
- CRUD operace pro účty, transakce a kategorie
- Autentizace a autorizace pomocí JWT
- Integrace s databází prostřednictvím JPA/Hibernate
- Výkonné dotazy a agregace dat

## Požadavky
Před spuštěním projektu se ujistěte, že máte nainstalovány následující nástroje:
- [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (doporučená verze: 11 nebo vyšší)
- [Maven](https://maven.apache.org/) pro správu závislostí
- Aktivní databázový server (např. PostgreSQL nebo MySQL)
- [Git](https://git-scm.com/) pro správu verzí

## Instalace a spuštění

### 1. Klonování repozitáře
Naklonujte tento repozitář do svého počítače:
```bash
git clone https://github.com/baiukov/fintrackServer.git
cd fintrackServer ```

### 2. Konfigurace prostředí
Tento projekt používá soubor .env nebo application.properties pro nastavení prostředí. Ujistěte se, že jste nastavili následující parametry:

Ukázka application.properties:

spring.datasource.url=jdbc:oracle://localhost:1521/fintrack
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect

jwt.secret=your_jwt_secret
jwt.expiration=86400

Pokud používáte jiný typ databáze (např. MySQL), upravte spring.datasource.url a hibernate.dialect podle dokumentace Hibernate.

### 3. Inicializace databáze
Spusťte váš databázový server a vytvořte databázi:
`CREATE DATABASE fintrack`;
Ujistěte se, že zadané přihlašovací údaje v application.properties odpovídají vašemu databázovému serveru.

### 4. Sestavení a spuštění aplikace
Použijte Maven k sestavení a spuštění aplikace:

mvn clean install
mvn spring-boot:run

Po úspěšném spuštění bude server naslouchat na http://localhost:8080. 

### Testování API
Pro testování API můžete použít nástroje jako Postman nebo Swagger. Zde je příklad dotazu na API:


### Další poznámky k instalaci
Bezpečnostní úložiště: Server používá JWT pro autentizaci. Ujistěte se, že jste nastavili jwt.secret ve vašem application.properties.
Interceptors: Server obsahuje interceptory pro zpracování bezpečnostních požadavků. Zkontrolujte, že vaše aplikace odpovídá požadavkům zabezpečení.
Struktura projektu
src/main/java: Obsahuje zdrojový kód aplikace, včetně konfigurací, kontrolerů, služeb a repozitářů.
src/main/resources: Obsahuje konfigurační soubory, jako je application.properties.
src/test/java: Obsahuje testovací třídy pro ověření funkcionality aplikace.

### Příspěvky
Pokud chcete přispět do tohoto projektu, neváhejte vytvořit pull request nebo nahlásit chybu v sekci Issues.

### Licence
Tento projekt je licencován pod licencí MIT.
