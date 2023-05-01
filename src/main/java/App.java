import java.sql.*;
import java.util.Date;
import java.util.Scanner;

public class App {
    // Estas son declaradas como variales globales ya que seran usadas en varios metodos.
    private static Connection connection;
    private static int id;
    private static String nombres, apePat, apeMat, fecNac, correo, ciudad;

    // Primero creamos una funcion para conectar con la base de datos
    public static Connection getConnection (String db, String user, String pass) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/"+db+"?user="+user+"&password="+pass;
        return DriverManager.getConnection(url);
    }

    public static void datosCliente(ResultSet rs) throws SQLException {
        // Creamos un carusel que muestre los datos de la table, este sera usado nuevamente en otros metodos pero no todos los elementos seran usados algunas veces.
        Date fecha = rs.getDate("fecha_nacimiento");
        id = Integer.parseInt(rs.getString("ID"));
        nombres = rs.getString("nombres");
        apePat = rs.getString("apellido_paterno");
        apeMat = rs.getString("apellido_materno");
        fecNac = fecha.toString();
        correo = rs.getString("correo");
        ciudad = rs.getString("ciudad");
    }

    // Se creo una funcion para mostrar los datos de un solo cliente a para mostrar resultados.
    public static void mostrarCliente(Integer idShow) throws SQLException {
        String queryMostrarCliente = "select * from cliente where ID = " + idShow;
        ResultSet resultSet = connection.createStatement().executeQuery(queryMostrarCliente);
        resultSet.next();
        datosCliente(resultSet);
        System.out.println("Id: " + id + " | Nombres: " + nombres + " | Apellido Paterno: " + apePat +
                " | Apellido Materno: " + apeMat + " | Fecha de Nacimiento: " + fecNac + " | Correo: " + correo + " | Ciudad: " + ciudad);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // Establecemos la conexion a la bd
        connection = getConnection("Develop", "root", "root");
        System.out.println("Conexion establecida...");

        Scanner scanner = new Scanner(System.in);

        // Aceptamos la opcion del usuario y ejecutamos el metodo correspondiete
        System.out.println("Ingrese la opcion que desea:\n 1. Listar Clientes \n 2. Actualizar correo de cliente \n " +
                "3. Crear registro de Cliente \n 4. Eliminar registro de cliente" );

        int opcion = scanner.nextInt();

        switch (opcion){
            // los dos primeros case hacen uso de Statements
            case 1:
                listarClientes(scanner);
                break;
            case 2:
                actualizarCliente(scanner);
                break;
            // los dos ultimos case hacen uso de PreparedStatements
            case 3:
                crearCliente(scanner);
                break;
            case 4:
                eliminarCliente(scanner);
                break;
            default:
                System.out.println("Opcion no implementada aun, elija una opcion de la lista");
                break;
        }
    }

    public static void listarClientes(Scanner scanner) throws SQLException {
        // Aqui usaremos un switch nuevamente para aceptar la opcion del usario
        System.out.println("Escoja una opcion: ");
        System.out.println("1. Listar todos los clientes");
        System.out.println("2. Listar los clientes de la ciudad de Lima");
        int opcion = scanner.nextInt();

        switch (opcion) {
            case 1:
                listarTodosClientes();
                scanner.close();
                break;
            case 2:
                listarClientesLima();
                scanner.close();
                break;
            default:
                System.out.println("Opcion no implementada aun, elija una opcion de la lista");
                break;
        }
    }

    // Este metodo nos muestra todos los registros de la tabla cliente.
    public static void listarTodosClientes() throws SQLException {
        // Se ejecuta un query que muestre todos los registros y los almacene en un Result Set.
        System.out.println("Estos son todos los clientes:");
        String queryAll = "SELECT * FROM cliente";
        ResultSet rs = connection.createStatement().executeQuery(queryAll);

        // Se implementa un carusel que muestra cada cliente en orden ascendente, e invoca el metodo mostrarCliente implementado anterioremente con el valor en su ID.
        int count = 1;
        while (rs.next()) {
            datosCliente(rs);
            System.out.println("Cliente " + count + ": ");
            mostrarCliente(Integer.parseInt(rs.getString("ID")));
            count++;
        }
        connection.close();
    }

    public static void listarClientesLima() throws SQLException {
        System.out.println("Estos son todos los clientes de Lima:");
        String queryClienteLima = "SELECT * FROM cliente where ciudad = 'Lima'";
        ResultSet rs = connection.createStatement().executeQuery(queryClienteLima);

        int count = 1;
        while (rs.next()) {
            datosCliente(rs);
            System.out.println("Cliente " + count + ": ");
            mostrarCliente(Integer.parseInt(rs.getString("ID")));
            count++;
        }
        connection.close();
    }

    // Este metodo recibe inforamcion y actualiza el correo de un cliente.
    public static void actualizarCliente(Scanner scanner) throws SQLException {
        // Con el scanner recibimos el ID del cliente y lo mostramos para que el usuario se asegure es el correcto.
        System.out.println("Ingrese el id del cliente a actualizar:");
        Integer idAct = scanner.nextInt();
        mostrarCliente(idAct);

        // Con el scanner nuevamente recibimos el nuevo correo a ingresar.
        System.out.println("Ingrese el nuevo correo del cliente a actualizar:");
        String correoAct = scanner.next();

        // Se ejecuta el query concatenando con la informacion del cliente.
        String queryActCorreo = "update cliente set correo = '"+ correoAct +"' where id = " + idAct;
        connection.createStatement().executeUpdate(queryActCorreo);

        // Se muestra la nueva informacion al usuario.
        System.out.println("Se ha actualizado el correo exitosamente.");
        mostrarCliente(idAct);
        scanner.close();
        connection.close();
    }

    // Este metodo crea un nuevo registro de cliente, sin ID ya que este se auto incrementa solo.
    public static void crearCliente(Scanner scanner) throws SQLException {
        // Con el scanner recibimos la informacion de cada campo y se almacena en variables.
        System.out.println("Ingrese los nombres del nuevo cliente");
        String newNombres = scanner.next();
        System.out.println("Ingrese el apellido paterno del nuevo cliente");
        String newApPaterno = scanner.next();
        System.out.println("Ingrese el apellido materno del nuevo cliente");
        String newApMaterno = scanner.next();
        System.out.println("Ingrese la fecha de nacimiento del nuevo cliente: (Formato YYYY-MM-DD , Años - Meses - Dias)");
        String newFechaNac = scanner.next();
        System.out.println("Ingrese el correo del nuevo cliente");
        String newCorreo = scanner.next();
        System.out.println("Ingrese la ciudad del nuevo cliente");
        String newCiudad = scanner.next();

        // Se ejecuta el query formado con la informacion recibida previamente en un prepared Statement.
        String queryInsert = "INSERT INTO cliente (nombres, apellido_paterno, apellido_materno, fecha_nacimiento, correo, ciudad) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(queryInsert);
        preparedStatement.setString(1, newNombres);
        preparedStatement.setString(2, newApPaterno);
        preparedStatement.setString(3, newApMaterno);
        preparedStatement.setString(4, newFechaNac);
        preparedStatement.setString(5, newCorreo);
        preparedStatement.setString(6, newCiudad);

        preparedStatement.execute();

        // Se muestra la nueva informacion al usuario, se encuentra el ultimo ID creado a partir de un nuevo query con un buscador sencillo.
        System.out.println("Se ha insertado el siguiente cliente: ");
        String queryLastId = "SELECT MAX(ID) FROM cliente";
        ResultSet rsLastId = connection.createStatement().executeQuery(queryLastId);
        rsLastId.next(); // de no usar esto nos mostraria el row incorrecto, siendo este 0 al parecer.
        mostrarCliente(Integer.parseInt(rsLastId.getString("MAX(ID)")));
        scanner.close();
        connection.close();
    }

    // Este ultimo metodo elimina un registro a partir del ID.
    public static void eliminarCliente(Scanner scanner) throws SQLException {
        // Con el scanner recibimos el ID del cliente y se almacena en una variable.
        System.out.println("Ingrese el id del cliente a eliminar:");
        Integer idElim = scanner.nextInt();

        // Se ejecuta el query concatenando con la informacion del cliente y se muestra el registro eliminado.
        mostrarCliente(idElim);
        String queryElim = "delete from cliente where id = " + idElim;
        connection.createStatement().executeUpdate(queryElim);
        System.out.println("Se ha eliminado el anterior cliente. ");
        scanner.close();
        connection.close();

        // A futuro se quiere copiar la informacion del cliente eliminado en una tabla aparte en la misma bd, pero no es requerido en este caso.
    }
}
