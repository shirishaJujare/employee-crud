package com.torryharris.employee.crud.verticles;


import com.torryharris.employee.crud.dao.Dao;
import com.torryharris.employee.crud.dao.impl.EmployeeJdbcDao;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.util.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class WorkerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(WorkerVerticle.class);
    private Dao<Employee> employeeDao;
    private Utils utils;

    public WorkerVerticle(Vertx vertx) {

        employeeDao = new EmployeeJdbcDao(vertx);
    }


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        DeploymentOptions workerOpts = new DeploymentOptions().setWorker(true);
        

       vertx.eventBus().consumer("worker",result->{
           String res = (String) result.body();
           LOGGER.info(res);
           Utils.getConnection(Long.parseLong(res));
       });

    }




}