package com.artificialsolutions.presales.jira;

import net.rcarz.jiraclient.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter Joles on 4/7/2016.
 * Company: Artificial Solutions
 */
public class ArtisolJiraClient {

    String userName;
    String password;
    String jiraUrl;


    JiraClient jira;

    public ArtisolJiraClient(String userName, String password, String jiraUrl) throws JiraException {
        this.userName = userName;
        this.password = password;
        this.jiraUrl = jiraUrl;

//        TokenCredentials creds = new TokenCredentials(userName, password);
        BasicCredentials creds = new BasicCredentials(userName, password);

        jira = new JiraClient(jiraUrl, creds);
    }

    public static void main(String[] args) {
        ArtisolJiraClient client;
        try {
            client = new ArtisolJiraClient("user.name", "your-password", "jiraUrl");
            String issueCode = client.createIssue("PJD", "Task", "jjj", "jjj", "jjj");
            System.out.println(issueCode);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all open tickets as a HTML table for a specific user name
     *
     * @param targetUserName
     * @return
     */
    public String getOpenTicketsForUserTable(String targetUserName, String projectCode, String doneStatusName) {
        try {
            // find all open tickets
            StringBuilder resultMessage = new StringBuilder();
            Issue.SearchResult searchResult = jira.searchIssues("reporter = " + targetUserName + " AND project = " + projectCode + " AND status NOT IN(" + doneStatusName + ")");

            if (searchResult.issues.size() > 0) {
                resultMessage.append("You have " + searchResult.issues.size() + " open tickets.<br/>");
                resultMessage.append("      <table class='jiraResults'>\n" +
                        "        <thead>\n" +
                        "          <tr>\n" +
                        "              <th data-field=\"id\">Key</th>\n" +
                        "              <th data-field=\"comment\">Last Comment</th>\n" +
                        "              <th data-field=\"status\">Status</th>\n" +
                        "              <th data-field=\"priority\">Priority</th>\n" +
                        "          </tr>\n" +
                        "        </thead>\n" +
                        "\n" +
                        "        <tbody>");
            }
            for (Issue i : searchResult.issues) {
                i.refresh("*all");
                List<Comment> comments = i.getComments();
                String lastComment = "No comments yet";
                if (comments.size() > 0) {
                    Comment comment = comments.get(comments.size() - 1);
                    lastComment = "<span class='commentAuthor'>" + comment.getAuthor().getDisplayName() + "</span> " + comment.getBody();
                }


                resultMessage
                        .append("<tr><td colspan='4' class='summaryHeader'>" + i.getSummary() + "</td></tr>\n")
                        .append("<tr><td><span class='commentId'><a href='#' onclick='DI.VA.hope.sendInput(\"Display JIRA Ticket " + i.getKey() + "\")'>" + i.getKey() + "</a></span></td>\n")
                        .append("<td>" + lastComment + "</td>\n")
                        .append("<td>" + i.getStatus().getName() + "</td>\n")
                        .append("<td>" + i.getPriority().getName() + "</td></tr>\n");
            }
            if (searchResult.issues.size() > 0) {
                resultMessage.append(" </tbody>\n" +
                        "      </table>\n");
            }
            return resultMessage.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public Issue getTicket(String ticketCode) {
        try {
            return jira.getIssue(ticketCode, "*all");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of open issues for a specific user in a specific project
     *
     * @param targetUserName
     * @return List<Issue>
     */
    public List<Issue> getOpenTicketsForUser(String targetUserName, String projectCode) {
        try {
            // find all open tickets
            StringBuilder resultMessage = new StringBuilder();
            Issue.SearchResult searchResult = jira.searchIssues("reporter = " + targetUserName + " AND project = " + projectCode + " AND status NOT IN(resolved, closed)");
            return searchResult.issues;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void changeTicketPriorityToCritical(String ticketCode) {
        try {
            Issue issue = jira.getIssue(ticketCode);
            issue.update()
                    .field(Field.PRIORITY, "Critical")
                    .execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void changeTicketPriorityToMajor(String ticketCode) {
        try {
            Issue issue = jira.getIssue(ticketCode);
            issue.update()
                    .field(Field.PRIORITY, "Major")
                    .execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a new JIRA ticket. Only works for projects that don't have complex custom fields.
     *
     * @param projectCode
     * @param issueType   "General" in most cases
     * @param summary
     * @param description
     * @param comment     Can be null
     * @return
     */
    public String createIssue(String projectCode, String issueType, String summary, String description, String comment) {
        try {
            Issue newIssue = jira.createIssue(projectCode, issueType)
                    .field(Field.SUMMARY, summary)
                    .field(Field.DESCRIPTION, description)
                    .execute();
            if (StringUtils.isNotBlank(comment)) {
                newIssue.addComment(comment);
            }
            return newIssue.getKey();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "PJD-1";
    }

    public boolean closeIssue(String issueCode, String comment, String status) {
        try {
            Issue issue = jira.getIssue(issueCode);
            issue.addComment(comment);
            issue.transition().execute(status);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
