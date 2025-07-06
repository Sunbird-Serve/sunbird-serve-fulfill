package com.sunbird.serve.fulfill.VolunteeringService.services;

import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.Need.Need;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import com.sunbird.serve.fulfill.models.request.NominationRequest;
import com.sunbird.serve.fulfill.models.request.NeedPlanRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequest;
import com.sunbird.serve.fulfill.models.request.UserStatusRequest;
import com.sunbird.serve.fulfill.models.Need.NeedPlan;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequirementRequest;


import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String getNCoordinatorEmailSubject() {
        return "New Volunteer Need Nomination: Action Required";
    }

    public String getNCoordinatorEmailBody(String nCoordinatorName, String nominatedUserName, String description) {
        return String.format(
            "Dear %s,<br><br>" +
            "A volunteer has shown interest in taking up a new class (Need) through the SERVE platform." +
            "<br><br>" +
            "<strong>Volunteer Name:</strong> %s" +
            "<br>" +
            "<strong>Nominated Need:</strong> %s" +
            "<br><br>" +
            "Kindly follow these steps:" +
            "<br>" +
            "Please log in to the portal" +
            "<br>" +
            "Click on the Need" +
            "<br>" +
            "Kindly Approve or Reject the nomination." +
            "<br><br>" +
            "Thank you for your support." +
            "<br><br>" +
            "Warm regards,<br>" +
            "Admin<br>" +
            "Sunbird SERVE",
            nCoordinatorName, nominatedUserName, description
        );
    }

    public String getNominatedUserEmailSubject() {
        return "Thank You for Your Nomination on SERVE";
    }

    public String getNominatedUserEmailBody(String nominatedUserName) {
        return String.format(
            "Dear %s,<br><br>" +
            "Thank you for nominating a new class (Need) on the SERVE platform." +
            "<br><br>" +
            "We truly appreciate your effort and your willingness to support children's learning. Your nomination helps us understand where support is needed, and we will review it soon." +
            "<br><br>" +
            "We will keep you updated on the next steps." +
            "<br><br>" +
            "Warm regards,<br>" +
            "Admin<br>" +
            "Sunbird SERVE",
            nominatedUserName
        );
    }

    public String getCancelSessionEmailSubject(String deliverableDate) {
        return String.format("Session on %s is cancelled", deliverableDate);
    }

    public String getCancelSessionEmailBody(String coordUserName, String deliverableDate, String comments,String needName, String assignedUserName, String assignedUserId) {
        return String.format(
            "Dear %s,<br><br>" +
            "This is to bring to your attention that the Volunteer has cancelled the session" +
            "<br><br>" +
            "Following are the Session Details" +
            "<br><br>" +
            "Session Date: %s" + "<br>" +
            "Need Name: %s" + "<br>" +
            "Volunteer Teacher Name: %s" + "<br>" +
            "Volunteer Teacher Id: %s" + "<br>" +
            "Reason for Cancellation:  %s" + "<br>" +
            "<br><br>" +
            "Kindly get in touch with the Volunteer and reschedule the session" +
            "<br><br>" +
            "Warm regards,<br>" +
            "Admin", coordUserName, deliverableDate, needName,  assignedUserName, assignedUserId, comments
        );
    }

    public String getVolunteerEmailSubject(NominationStatus status) {
        if (status == NominationStatus.Approved) {
            return "Confirmation: Your Volunteer Need Nomination";
        } else if (status == NominationStatus.Rejected) {
            return "Update on Your Volunteer Need Nomination";
        }
        return "Update on Your Volunteer Need Nomination";
    }

    public String getVolunteerEmailBody(String nominatedUserName, NominationStatus status, String description, String entityName, String coordUserName, String coordPhoneNumber) {
        if (status == NominationStatus.Approved) {
            return String.format(
                "Dear %s,<br><br>" +
                "We are delighted to share that your need nomination on the SERVE platform has been approved by the school teacher." +
                "<br><br>" +
                "<strong>Volunteer Need:</strong> %s" +
                "<br>" +
                "<strong>School Name:</strong> %s" +
                "<br>" +
                "<strong>School Coordinator Name:</strong> %s" +
                "<br>" +
                "<strong>School Coordinator Phone:</strong> %s" +
                "<br><br>" +
                "Please log in to the platform to access your class schedule." +
                "<br>" +
                "Wishing you all the best as you embark on these classes." +
                "<br><br>" +
                "Warm regards,<br>" +
                "Admin<br>" +
                "Sunbird SERVE",
                nominatedUserName, description, entityName, coordUserName, coordPhoneNumber
            );
        } else if (status == NominationStatus.Rejected) {
            return String.format(
                "Dear %s,<br><br>" +
                "Thank you for nominating a class (Need) on the SERVE platform." +
                "<br><br>" +
                "We wanted to let you know that this Need has already been assigned to another volunteer. Request you to nominate another Need that matches your interest and availability." +
                "<br><br>" +
                "Thank you for your continued support!" +
                "<br><br>" +
                "Warm regards,<br>" +
                "Admin",
                nominatedUserName
            );
        }
        return "";
    }
}
