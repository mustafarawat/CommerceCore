package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.ContactMessageRequestDTO;
import com.e.commerce.mini.DTO.response.ContactMessageResponseDTO;
import com.e.commerce.mini.Repository.ContactMessageRepository;
import com.e.commerce.mini.Service.ContactMessageService;
import com.e.commerce.mini.models.ContactMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    @Override
    public ContactMessageResponseDTO createContactMessage(ContactMessageRequestDTO request) {

        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setName(request.getName());
        contactMessage.setEmail(request.getEmail());
        contactMessage.setPhone(request.getPhone());
        contactMessage.setSubject(request.getSubject());
        contactMessage.setMessage(request.getMessage());

        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);

        sendAdminEmail(savedMessage);

        return new ContactMessageResponseDTO(
                savedMessage.getId(),
                savedMessage.getName(),
                savedMessage.getEmail(),
                savedMessage.getPhone(),
                savedMessage.getSubject(),
                savedMessage.getMessage(),
                savedMessage.getStatus(),
                savedMessage.getCreatedAt()
        );
    }

    private void sendAdminEmail(ContactMessage message) {

        try {
            MimeMessage mail = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mail,
                    false,
                    "UTF-8"
            );

            helper.setTo(adminEmail);
            helper.setSubject(
                    "New Customer Inquiry - " + safeValue(message.getSubject())
            );

            String html =
                    "<!DOCTYPE html>"
                            + "<html>"
                            + "<body style=\"margin:0;padding:0;background:#F1EEE6;font-family:Arial,sans-serif;\">"

                            + "<div style=\"max-width:650px;margin:30px auto;background:#FCF9F3;border:1px solid #DDD5C9;border-radius:14px;overflow:hidden;\">"

                            + "<div style=\"padding:28px 30px;background:#39483F;color:#FCF9F3;\">"
                            + "<div style=\"font-size:24px;font-weight:bold;\">CommerceCore</div>"
                            + "<div style=\"margin-top:6px;font-size:11px;letter-spacing:2px;color:#C7AE82;\">CUSTOMER CARE</div>"
                            + "</div>"

                            + "<div style=\"padding:30px;\">"

                            + "<h2 style=\"margin:0 0 8px;color:#39483F;font-size:22px;\">"
                            + "New Customer Inquiry"
                            + "</h2>"

                            + "<p style=\"margin:0 0 25px;color:#718477;font-size:14px;line-height:1.6;\">"
                            + "A new message has been submitted through the CommerceCore Contact Us page."
                            + "</p>"

                            + "<div style=\"border:1px solid #DDD5C9;border-radius:10px;overflow:hidden;\">"

                            + "<div style=\"padding:14px 18px;background:#F8F4ED;border-bottom:1px solid #E7E0D6;\">"
                            + "<strong style=\"font-size:11px;letter-spacing:1.5px;color:#9A8F80;\">CUSTOMER DETAILS</strong>"
                            + "</div>"

                            + "<div style=\"padding:18px;\">"

                            + "<p style=\"margin:0 0 12px;color:#39483F;font-size:14px;\">"
                            + "<strong>Name:</strong> "
                            + escapeHtml(message.getName())
                            + "</p>"

                            + "<p style=\"margin:0 0 12px;color:#39483F;font-size:14px;\">"
                            + "<strong>Email:</strong> "
                            + escapeHtml(message.getEmail())
                            + "</p>"

                            + "<p style=\"margin:0 0 12px;color:#39483F;font-size:14px;\">"
                            + "<strong>Phone:</strong> "
                            + escapeHtml(message.getPhone())
                            + "</p>"

                            + "<p style=\"margin:0 0 12px;color:#39483F;font-size:14px;\">"
                            + "<strong>Subject:</strong> "
                            + escapeHtml(message.getSubject())
                            + "</p>"

                            + "<p style=\"margin:0;color:#39483F;font-size:14px;\">"
                            + "<strong>Date:</strong> "
                            + escapeHtml(String.valueOf(message.getCreatedAt()))
                            + "</p>"

                            + "</div>"
                            + "</div>"

                            + "<div style=\"margin-top:18px;border:1px solid #DDD5C9;border-radius:10px;overflow:hidden;\">"

                            + "<div style=\"padding:14px 18px;background:#F8F4ED;border-bottom:1px solid #E7E0D6;\">"
                            + "<strong style=\"font-size:11px;letter-spacing:1.5px;color:#9A8F80;\">MESSAGE</strong>"
                            + "</div>"

                            + "<div style=\"padding:20px 18px;color:#718477;font-size:14px;line-height:1.7;white-space:pre-wrap;\">"
                            + escapeHtml(message.getMessage())
                            + "</div>"

                            + "</div>"

                            + "<div style=\"margin-top:24px;padding:16px 18px;background:#39483F;border-radius:9px;color:#FCF9F3;font-size:12px;line-height:1.6;\">"
                            + "Please review this inquiry from the Customer Care section in the CommerceCore Admin Console."
                            + "</div>"

                            + "</div>"

                            + "<div style=\"padding:18px 30px;border-top:1px solid #E7E0D6;color:#9A8F80;font-size:11px;text-align:center;\">"
                            + "CommerceCore Customer Care"
                            + "</div>"

                            + "</div>"

                            + "</body>"
                            + "</html>";

            helper.setText(html, true);

            javaMailSender.send(mail);

            System.out.println("Customer inquiry email sent successfully.");

        } catch (Exception exception) {
            System.out.println(
                    "Customer inquiry email could not be sent: "
                            + exception.getMessage()
            );
        }
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    public List<ContactMessageResponseDTO> getAllContactMessages() {

        return contactMessageRepository.findAll()
                .stream()
                .map(message -> new ContactMessageResponseDTO(
                        message.getId(),
                        message.getName(),
                        message.getEmail(),
                        message.getPhone(),
                        message.getSubject(),
                        message.getMessage(),
                        message.getStatus(),
                        message.getCreatedAt()
                ))
                .toList();
    }
}