package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTestTicket(Ticket ticket){
        Connection con=null;
        try {
            con=dataBaseConfig.getConnection();
            PreparedStatement ps= con.prepareStatement(DBConstants.SAVE_TICKET);
            ps.setInt(1,ticket.getId());
            ps.setInt(2,ticket.getParkingSpot().getId());
            ps.setString(3, ticket.getVehicleRegNumber());
            ps.setDouble(4, ticket.getPrice());
            ps.setTimestamp(5, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(6, new Timestamp(ticket.getOutTime().getTime()));
            return ps.execute();
        }catch (Exception ex){
            logger.error("Error Saving Test Ticket",ex);
            return false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    public boolean UpdateIntimeTestTicket(Ticket ticket){
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TEST_IN_TIME);
            ps.setTimestamp(1, new Timestamp(ticket.getInTime().getTime()));
            ps.setInt(2, ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error updating in time ticket info",ex);
            return false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    public int getNextAvailableTicketID(){
        Connection con=null;
        int result=0;
        try{
            con= dataBaseConfig.getConnection();
            PreparedStatement ps= con.prepareStatement(DBConstants.TOTAL_NB_TICKET);
            ResultSet rs=ps.executeQuery();
            rs.next();
            result=rs.getInt("result");
        }
        catch(Exception ex){
            logger.error("Error counting number of ticket for this vehicle",ex);
        }finally{
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }


    public boolean saveTicket(Ticket ticket){
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setInt(1,ticket.getId());
            ps.setInt(2,ticket.getParkingSpot().getId());
            ps.setString(3, ticket.getVehicleRegNumber());
            ps.setDouble(4, ticket.getPrice());
            ps.setTimestamp(5, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(6, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            return ps.execute();
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
            return false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
            return false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    public int getNbTicket(String vehicleRegNumber) {
        Connection con=null;
        int result=0;
        try{
            con= dataBaseConfig.getConnection();
            PreparedStatement ps= con.prepareStatement(DBConstants.NUMBER_TICKET);
            ps.setString(1,vehicleRegNumber);
            ResultSet rs=ps.executeQuery();
            rs.next();
            result=rs.getInt("result");
        }
        catch(Exception ex){
            logger.error("Error counting number of ticket for this vehicle",ex);
        }finally{
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }
}
