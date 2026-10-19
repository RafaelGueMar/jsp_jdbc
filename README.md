# Gestión de socios

Aplicación web Java para gestionar socios de un club de baloncesto. La interfaz y las operaciones se implementan con Jakarta Servlets; los datos se consultan mediante JDBC.

## Requisitos

- JDK 25
- Maven
- Un contenedor compatible con Jakarta Servlet 6, como Tomcat 10.1
- MySQL

## Base de datos

Importa `src/main/java/baloncesto.sql` en MySQL. Por defecto, la aplicación se conecta a `jdbc:mysql://localhost:3306/baloncesto` con el usuario `root` y la contraseña `root`.

Se pueden configurar las credenciales con las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.

## Compilar y desplegar

```sh
mvn clean package
```

Despliega `target/jsp-jdbc.war` en el contenedor Servlet. Las rutas disponibles son `/`, `/formularioSocio`, `/grabaSocio`, `/listadoSocios`, `/pideNumeroSocio` y `/borraSocio`.
