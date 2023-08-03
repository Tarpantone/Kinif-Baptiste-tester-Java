package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    //*test de l’appel de la méthode
    //processExitingVehicle() où tout se déroule comme attendu.
    @Test
    public void processExitingVehicleTest(){
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO,times(1)).getTicket(anyString());
        verify(ticketDAO,times(1)).getNbTicket(anyString());
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
    }

    //*test de l’appel de la méthode
    //processIncomingVehicle() où tout se déroule comme attendu.

    @Test
    public void testProcessIncomingVehicle(){
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(inputReaderUtil,times(1)).readSelection();
        verify(ticketDAO,times(1)).getNbTicket(anyString());
        verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(ticketDAO,times(1)).saveTicket(any(Ticket.class));
    }

    //*exécution du test dans
        // le cas où la méthode updateTicket() de ticketDAO renvoie false
        //lors de l’appel de processExitingVehicle()

    @Test
    public void processExitingVehicleTestUnableUpdate(){
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

        parkingService.processExitingVehicle();

        verify(ticketDAO,times(1)).getTicket(anyString());
        verify(ticketDAO,times(1)).getNbTicket(anyString());
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
    }

    //*test de l’appel de la
        //méthode getNextParkingNumberIfAvailable() avec pour résultat
        //l’obtention d’un spot dont l’ID est 1 et qui est disponible.

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot result=parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil,times(1)).readSelection();

        ParkingSpot expected=new ParkingSpot(1,ParkingType.CAR,true);
        assertEquals(expected,result);
    }

    //*test de l’appel de la méthode getNextParkingNumberIfAvailable()
        //avec pour résultat aucun spot disponible (la méthode renvoie null).

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot result=parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil,times(1)).readSelection();

        ParkingSpot expected=null;
        assertEquals(expected,result);
    }

    //*test de l’appel de la méthode
        //getNextParkingNumberIfAvailable() avec pour résultat aucun spot
        //(la méthode renvoie null) car l’argument saisi par l’utilisateur
        //concernant le type de véhicule est erroné (par exemple,
        //l’utilisateur a saisi 3.

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot result=parkingService.getNextParkingNumberIfAvailable();

        verify(inputReaderUtil,times(1)).readSelection();

        ParkingSpot expected=null;
        assertEquals(expected,result);
    }

}
