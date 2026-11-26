# SISIM - Inventario de maquillaje (Java + MySQL)

Proyecto académico sencillo de consola. Solo **Administrador** y **Empleado** usan el sistema.

## Requisitos
- Java JDK 17 o superior.
- MySQL 8 o superior.
- Maven 3.9 o superior (o VS Code con soporte Maven).

## 1. Crear base de datos
En MySQL Workbench abre y ejecuta `base_datos.sql` (incluye `CREATE DATABASE sisim`).

## 2. Configurar conexión
Por defecto usa `localhost:3306`, usuario `root` y contraseña vacía.
Para configurar una contraseña, en PowerShell, dentro de la terminal donde ejecutarás Maven:

```powershell
$env:SISIM_DB_USER="root"
$env:SISIM_DB_PASSWORD="TU_CLAVE_MYSQL"
```

Si usas otro puerto, también configura `$env:SISIM_DB_URL="jdbc:mysql://localhost:3307/sisim?useSSL=false&serverTimezone=UTC"`.

**No subas contraseñas a GitHub.**

## 3. Ejecutar
Abre esta carpeta en VS Code y ejecuta en su terminal:

```bash
mvn compile exec:java
```

La primera vez el programa solicita crear un **Administrador** con contraseña propia.
Luego inicia sesión y usa la opción **6. Crear empleado** para agregar usuarios de tipo empleado.

## Roles
- **Administrador:** consultar inventario, registrar productos, agregar existencias, registrar proveedores, crear empleados, ver ventas, ver bajo stock y registrar ventas.
- **Empleado:** consultar inventario y registrar ventas.
- Clientes y proveedores **no** ingresan al programa.

## Limitaciones conscientes de la versión 1
- Una venta registra **un producto y una cantidad**; se puede repetir para otros productos. Para un carrito con varios productos en una sola venta se necesita una ampliación.
- No incluye pantalla gráfica, vencimientos ni compras detalladas.
- `RegistroVentaFacade` ilustra el patrón Facade; las ventas usan transacciones para evitar descontar inventario sin registrar la venta.
- Se usan consultas preparadas para evitar inyección SQL y hashes PBKDF2 para las contraseñas.
