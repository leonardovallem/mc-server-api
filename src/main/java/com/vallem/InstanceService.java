package com.vallem;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.vallem.model.InstanceState;

public class InstanceService {
    final static AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
    final static String INSTANCE_ID = System.getenv("INSTANCE_ID");

    public static void startEC2Instance() {
        final var request = new StartInstancesRequest()
                .withInstanceIds(INSTANCE_ID);
        ec2.startInstances(request);
    }

    public static void stopEC2Instance() {
        final var request = new StopInstancesRequest()
                .withInstanceIds(INSTANCE_ID);
        ec2.stopInstances(request);
    }

    public static void restartEC2Instance() {
        final var request = new RebootInstancesRequest()
                .withInstanceIds(INSTANCE_ID);
        ec2.rebootInstances(request);
    }

    public static Instance getInstance() {
        boolean done = false;
        final var request = new DescribeInstancesRequest();

        while (!done) {
            final var response = ec2.describeInstances(request);

            for (Reservation reservation : response.getReservations()) {
                final var opt = reservation.getInstances()
                        .stream()
                        .filter(inst -> inst.getInstanceId().equals(INSTANCE_ID))
                        .findFirst();

                if (opt.isPresent()) return opt.get();
            }

            request.setNextToken(response.getNextToken());
            if (response.getNextToken() == null) done = true;
        }

        return null;
    }

    public static InstanceState getEC2InstanceState() {
        final var instance = getInstance();
        if (instance == null) return null;

        return new InstanceState(instance.getState().getName());
    }
}
