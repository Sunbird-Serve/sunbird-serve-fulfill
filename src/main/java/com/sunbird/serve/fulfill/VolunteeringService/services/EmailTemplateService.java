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
            "This is to bring to your attention a new volunteer need that has been nominated by one of our dedicated volunteers through the SERVE platform." +
            "<br><br>" +
            "<strong>Volunteer Name:</strong> %s" +
            "<br>" +
            "<strong>Nominated Need:</strong> %s" +
            "<br><br>" +
            "Please take a moment to review the nominated need and provide your feedback or decision. Your prompt attention to this matter is greatly appreciated." +
            "<br><br>" +
            "Thank you for your continued dedication to our mission and for your support in making SERVE a platform that truly makes a difference in people's lives." +
            "<br><br>" +
            "Warm Regards,<br>" +
            "Admin",
            nCoordinatorName, nominatedUserName, description
        );
    }

    public String getNominatedUserEmailSubject() {
        return "Your Volunteer Need Nomination - Thank You!";
    }

    public String getNominatedUserEmailBody(String nominatedUserName) {
        return String.format(
            "Dear %s,<br><br>" +
            "We hope this message finds you well and filled with the same enthusiasm that you bring to our volunteer community every day." +
            "<br><br>" +
            "We wanted to take a moment to express our sincere gratitude for your recent nomination of a volunteer need through SERVE." +
            "<br><br>" +
            "Your nomination is a vital contribution to our efforts to better serve our community and address its needs effectively. We're eager to review your nomination." +
            "<br><br>" +
            "Thank you once again for your commitment and passion for serving others. We look forward to exploring your nomination further and keeping you updated on its progress." +
            "<br><br>" +
            "Warm regards,<br>" +
            "Admin",
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

    public String getVolunteerEmailBody(String nominatedUserName, NominationStatus status, String description) {
        if (status == NominationStatus.Approved) {
            return String.format(
                "Dear %s,<br><br>" +
                "I hope this message finds you in good spirits." +
                "<br><br>" +
                "I'm delighted to share that your volunteer need nomination on the SERVE platform has been carefully reviewed and approved by our administrative team." +
                "<br><br>" +
                "<strong>Volunteer Need:</strong> %s" +
                "<br><br>" +
                "Your commitment to empowering rural children's education is deeply appreciated and highly valued." +
                "<br>" +
                "Please log in to the platform to access the Need Plan, where you'll find detailed information about the sessions and schedule." +
                "<br><br>" +
                "Wishing you all the best as you embark on these classes." +
                "<br><br>" +
                "Warm regards,<br>" +
                "Admin",
                nominatedUserName, description
            );
        } else if (status == NominationStatus.Rejected) {
            return String.format(
                "Dear %s,<br><br>" +
                "I hope this email finds you well." +
                "<br><br>" +
                "I wanted to provide you with an update regarding your recent volunteer need nomination on the SERVE platform. After careful consideration, our administrative team has reviewed your suggestion, and unfortunately, we have decided not to proceed with the nomination at this time." +
                "<br><br>" +
                "While we deeply appreciate your initiative and dedication to making a positive impact, upon evaluation, we found that the nominated need may not align perfectly with our current timelines and required skill sets." +
                "<br><br>" +
                "Please understand that your commitment to serving others is immensely valued, and we encourage you to continue exploring opportunities that better match your availability and skills." +
                "<br><br>" +
                "If you have any questions or would like further clarification on our decision, please don't hesitate to reach out. We're here to support you in any way we can." +
                "<br><br>" +
                "Thank you for your understanding and ongoing support of our mission." +
                "<br><br>" +
                "Warm regards,<br>" +
                "Admin",
                nominatedUserName
            );
        }
        return "";
    }
}
