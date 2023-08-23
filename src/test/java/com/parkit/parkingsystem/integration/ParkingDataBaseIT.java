package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
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

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
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
        dataBasePrepareService.clearDataBaseEntries();
    }

    @BeforeEach
   public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
    }

    @AfterAll
    public static void tearDown(){

    }

   @Test
    public void testParkingACar() throws Exception {
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

        parkingService.processIncomingVehicle();
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Ticket exit= ticketDAO.getTicket("EXIT");
        exit.setInTime(inTime);

        System.out.println(new Timestamp(exit.getInTime().getTime()));

        assertTrue(ticketDAO.UpdateIntimeTestTicket(exit));
        parkingService.processExitingVehicle();

        String vehicleRegNumber="EXIT";
        Ticket result= ticketDAO.getTicket(vehicleRegNumber);

        double expected1=1.5;
        boolean expected2=true;
        assertEquals(expected1, result.getPrice());
        assertEquals(expected2, result.getOutTime()!=null);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("REGULAR");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Date regularIntime=new Date();
        regularIntime.setTime(System.currentTimeMillis()-(3*60*60*1000));
        Date regularOutime=new Date();
        regularOutime.setTime(System.currentTimeMillis()-(2*60*60*1000));
        ParkingSpot slot=parkingService.getNextParkingNumberIfAvailable();

        Ticket regular=new Ticket();
        regular.setInTime(regularIntime);
        regular.setParkingSpot(slot);
        regular.setOutTime(regularOutime);
        regular.setPrice(1.5);
        regular.setId(ticketDAO.getNextAvailableTicketID());
        regular.setVehicleRegNumber("REGULAR");

        assertFalse(ticketDAO.saveTestTicket(regular));

        parkingService.processIncomingVehicle();
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Ticket currentRegular= ticketDAO.getTicket("REGULAR");
        currentRegular.setInTime(inTime);
        assertTrue(ticketDAO.UpdateIntimeTestTicket(currentRegular));
        parkingService.processExitingVehicle();
        double result=ticketDAO.getTicket("REGULAR").getPrice();

        double expected=1.43;
        assertEquals(expected,result);
    }

}
