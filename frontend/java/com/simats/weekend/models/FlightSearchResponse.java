package com.simats.weekend.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FlightSearchResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    // --- NESTED CLASSES TO MATCH THE JSON ---

    public static class Data {
        @SerializedName("flightQuotes")
        private FlightQuotes flightQuotes;

        public FlightQuotes getFlightQuotes() {
            return flightQuotes;
        }
    }

    public static class FlightQuotes {
        @SerializedName("results")
        private List<Result> results;

        public List<Result> getResults() {
            return results;
        }
    }

    public static class Result {
        @SerializedName("content")
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        @SerializedName("rawPrice")
        private double rawPrice;

        @SerializedName("outboundLeg")
        private OutboundLeg outboundLeg;

        public double getRawPrice() {
            return rawPrice;
        }

        public OutboundLeg getOutboundLeg() {
            return outboundLeg;
        }
    }

    public static class OutboundLeg {
        @SerializedName("localDepartureDateLabel")
        private String localDepartureDateLabel;

        @SerializedName("originAirport")
        private Airport originAirport;

        @SerializedName("destinationAirport")
        private Airport destinationAirport;

        public String getLocalDepartureDateLabel() {
            return localDepartureDateLabel;
        }

        public Airport getOriginAirport() {
            return originAirport;
        }

        public Airport getDestinationAirport() {
            return destinationAirport;
        }
    }

    public static class Airport {
        @SerializedName("skyCode")
        private String skyCode;

        public String getSkyCode() {
            return skyCode;
        }
    }
}