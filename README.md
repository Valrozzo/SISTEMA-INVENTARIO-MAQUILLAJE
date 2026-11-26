# SISIM - Sistema de Inventario para Tienda de Maquillaje

Aplicación académica existente con frontend HTML/CSS/JavaScript, servidor HTTP y JDBC en Java 17, y MySQL. Conserva los módulos de usuarios, productos, inventario, proveedores, ventas y alertas de vencimiento.

## Requisitos

- JDK 17 o superior.
- Maven 3.9 o superior.
- MySQL compatible, por ejemplo el servicio MySQL de XAMPP.

## Preparar el primer inicio

1. Inicie MySQL desde el panel de XAMPP.
2. Desde PowerShell, en la carpeta del proyecto, configure la conexión. Los valores siguientes son un ejemplo para XAMPP local; use el usuario y la contraseña que realmente tenga su servidor:

   ```powershell
   $env:SISIM_DB_URL = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false"
   $env:SISIM_DB_USER = Read-Host "Usuario de MySQL"
   $env:SISIM_DB_PASSWORD = Read-Host "Contraseña de MySQL"
   $env:SISIM_BOOTSTRAP_ADMIN_USER = Read-Host "Usuario del administrador inicial"
   $env:SISIM_BOOTSTRAP_ADMIN_PASSWORD = Read-Host "Contraseña del administrador inicial"
   ```

   Para iniciar desde la configuración Java de VS Code, copie `.env.example` a `.env` y ajuste los valores de MySQL. `.env` está excluido de Git para no publicar configuración local ni contraseñas.

3. La aplicación ejecuta `database/base_datos.sql` al iniciar y crea la base `sisim` y las tablas declaradas allí. El usuario MySQL necesita permisos para crear/usar la base y tablas.
4. En el primer inicio, si todavía no existe un usuario administrador, SISIM crea uno con las variables `SISIM_BOOTSTRAP_ADMIN_USER` y `SISIM_BOOTSTRAP_ADMIN_PASSWORD`. El nombre debe tener entre 3 y 50 caracteres (`A-Z`, `a-z`, dígitos, punto, guion y guion bajo); la contraseña debe tener entre 8 y 200 caracteres. Las variables no se guardan en el repositorio. Después de crear el usuario, quite esas dos variables de la sesión.
5. Si la base ya tiene un administrador, el mecanismo de inicialización no modifica cuentas existentes.

> `useSSL=false` corresponde únicamente al ejemplo local de XAMPP. No use esta configuración en una conexión de red de producción; esta aplicación académica tampoco configura HTTPS.

## Ejecutar y probar

```powershell
mvn clean test
mvn exec:java
```

Abra <http://localhost:8080>. `SISIM_PORT` permite configurar otro puerto. Para la ejecución normal también deben estar definidas `SISIM_DB_URL`, `SISIM_DB_USER` y `SISIM_DB_PASSWORD` en el entorno de la aplicación.

Las pruebas unitarias no requieren una instancia MySQL. Las operaciones de acceso, inventario, proveedores y ventas sí dependen de la conexión real a MySQL.

## Roles

- **Administrador:** registra productos y proveedores, administra usuarios, consulta inventario y registra ventas.
- **Empleado:** consulta el inventario y registra ventas.

El registro público crea exclusivamente cuentas de empleado. Los clientes y proveedores no acceden al sistema.

Las contraseñas nuevas usan PBKDF2-HMAC-SHA256 con salt aleatorio. Las contraseñas antiguas en texto simple se convierten al formato hash en el siguiente inicio de sesión correcto; evite guardar credenciales antiguas y cámbielas. Las sesiones son locales al proceso y caducan a las ocho horas.

## Documentación de calidad

La revisión, métricas reproducibles, problemas corregidos, pruebas, limitaciones y mantenimiento se describen en [docs/guia-practica-11.md](docs/guia-practica-11.md) y [docs/mantenimiento.md](docs/mantenimiento.md).
