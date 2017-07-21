package com.github.gavlyukovskiy.sample;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
@RequestMapping("/select")
public class SelectController {

    private final DataSource dataSource;

    public SelectController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @RequestMapping("/")
    public String run() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT NOW()")) {
            resultSet.next();
            return resultSet.getString(1);
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }
}
