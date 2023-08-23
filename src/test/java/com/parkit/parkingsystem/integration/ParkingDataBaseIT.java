package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    /* @BeforeEach
   public void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
    }*/

    @AfterAll
    public static void tearDown(){

    }

   @Test
    public void testParkingACar() throws Exception {

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("INCOMING");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        String vehicleRegNumber="INCOMING";
        Ticket result = ticketDAO.getTicket(vehicleRegNumber);
        boolean expected1 = true;
        boolean expected2=false;

        assertEquals(expected1,result!=null);
        assertEquals(expected2,result.getParkingSpot().isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("EXIT");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processExitingVehicle();

        String vehicleRegNumber="EXIT";
        Ticket result= ticketDAO.getTicket(vehicleRegNumber);

        boolean expected1=true;
        boolean expected2=true;
        //TODO: check that the fare generated and out time are populated correctly in the database
        assertEquals(expected1, result.getPrice()!=0.00);
        assertEquals(expected2, result.getOutTime()!=null);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("REGULAR");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
    }

}
