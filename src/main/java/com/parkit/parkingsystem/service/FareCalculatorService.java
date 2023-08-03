package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;



public class FareCalculatorService {

    public void calculateFare (Ticket ticket){
        calculateFare(ticket,false);
    }
    public void calculateFare(Ticket ticket,boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Long inTime= Long.valueOf(ticket.getInTime().getTime());
        Long outTime= Long.valueOf(ticket.getOutTime().getTime());
        Long timeBetween= Long.valueOf(outTime-inTime);

        double duration = timeBetween.doubleValue()/3600000;

        if (duration<=0.5){

            ticket.setPrice(0.00);

        }else if(discount){
            double price;
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    price=duration * Fare.CAR_RATE_PER_HOUR*0.95;
                    price=Math.round(price*100.00)/100.00;
                    ticket.setPrice(price);
                    break;
                }
                case BIKE: {
                    price=duration * Fare.BIKE_RATE_PER_HOUR*0.95;
                    price=Math.round(price*100.00)/100.00;
                    ticket.setPrice(price);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }

        }else {
            double price;
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    price=duration * Fare.CAR_RATE_PER_HOUR;
                    price=Math.round(price*100.00)/100.00;
                    ticket.setPrice(price);
                    break;
                }
                case BIKE: {
                    price=duration * Fare.BIKE_RATE_PER_HOUR;
                    price=Math.round(price*100.00)/100.00;
                    ticket.setPrice(price);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}