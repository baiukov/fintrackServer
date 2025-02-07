package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.ATest;
import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.repositories.UserRepository;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTestOld extends ATest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private IDatabaseConnection dbConnection;

    @BeforeEach
    public void setUp() throws Exception {
//        IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
//        IDataSet dataSet = new XmlDataSet(getClass().getClassLoader().getResourceAsStream("datasets/defaultSet.xml"));
//        connection.getConnection().createStatement().execute("DELETE FROM users");
        jdbcTemplate.execute("SET SCHEMA test_schema");
        Connection connection = dataSource.getConnection();
//        connection.setSchema("test_schema");
        dbConnection = new DatabaseConnection(connection);
        // Очистка таблицы перед тестом
        dbConnection.getConnection().createStatement().execute("DELETE FROM test_schema.users");
    }

    @Test
    public void registerUserTest() throws Exception {
        // Регистрация пользователя
        User registeredUser = userService.registerUser("abc@bc.c", "John", "1234568Aa");

        // Проверяем, что пользователь был успешно зарегистрирован
        assertNotNull(registeredUser);

        // Загружаем ожидаемое состояние из файла
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder()
                .build(getClass().getClassLoader().getResourceAsStream("datasets/defaultSet.xml"));
        ITable expectedTable = expectedDataSet.getTable("Users");


        ResultSet resultSet = dbConnection.getConnection().createStatement().executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'test_schema'");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("table_name"));
        }
        // Получаем актуальное состояние таблицы users из базы данных
        IDataSet actualDataSet = dbConnection.createDataSet();
        ITable actualTable = actualDataSet.getTable("asdasd");

        // Сравниваем таблицы
        new DbUnitAssert().assertEquals(expectedTable, actualTable);
    }




}
