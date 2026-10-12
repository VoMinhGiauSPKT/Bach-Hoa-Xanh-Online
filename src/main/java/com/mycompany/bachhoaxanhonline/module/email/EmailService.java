package com.mycompany.bachhoaxanhonline.module.email;

import com.mycompany.bachhoaxanhonline.entity.LineItem;
import com.mycompany.bachhoaxanhonline.entity.Order;
import com.mycompany.bachhoaxanhonline.entity.Product;
import com.mycompany.bachhoaxanhonline.util.ConfigUtil;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service quản lý gửi email qua Google Apps Script Webhook (Port 443 HTTPS).
 * Cơ chế gửi thuần Webhook với chuẩn HTML Email tương thích 100% mọi ứng dụng thư
 * (Gmail, Outlook, Apple Mail), sử dụng font chữ mượt mà, layout table an toàn và
 * không dùng emoji phức tạp tránh lỗi font chữ / ký tự hỏi chấm (?).
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss 'ngày' dd/MM/yyyy");

    // Webhook URL của Google Apps Script chạy qua cổng 443 HTTPS (Không bị Render chặn, không yêu cầu App Password)
    private static final String DEFAULT_WEBHOOK_URL = "https://script.google.com/macros/s/AKfycbwBGh_6dRvBKn-v9nfcRQuhjJJv4DXFaIWOLSFrc3fpB-zk_3Ox0x_d3UuJqC7z_br17Q/exec";

    // Thread pool chuyên xử lý gửi email trong nền
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread thread = new Thread(r, "EmailSender-Thread");
        thread.setDaemon(true);
        return thread;
    });

    private static EmailService instance;

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Gửi email dạng HTML bất đồng bộ (Async qua Thread Pool)
     */
    public void sendEmailAsync(String toEmail, String subject, String htmlContent) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            LOGGER.warning(">>> [EmailService] KHÔNG THỂ GỬI: Địa chỉ email người nhận rỗng!");
            return;
        }

        LOGGER.info(">>> [EmailService] Đang xếp hàng gửi email tới: " + toEmail + " | Tiêu đề: " + subject);

        EMAIL_EXECUTOR.submit(() -> {
            try {
                sendEmailSync(toEmail.trim(), subject, htmlContent);
                LOGGER.info(">>> [EmailService] GỬI EMAIL THÀNH CÔNG TỚI: " + toEmail);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, ">>> [EmailService] LỖI GỬI EMAIL TỚI " + toEmail + ": " + e.getMessage(), e);
            }
        });
    }

    /**
     * Gửi email dạng HTML đồng bộ thuần qua Google Apps Script Webhook (Port 443 HTTPS)
     */
    public void sendEmailSync(String toEmail, String subject, String htmlContent) throws Exception {
        sendViaWebhook(toEmail, subject, htmlContent, true);
    }

    /**
     * Gửi request POST JSON tới Google Apps Script Webhook qua HTTP/HTTPS
     */
    private void sendViaWebhook(String toEmail, String subject, String body, boolean isHTML) throws Exception {
        String webhookUrl = ConfigUtil.get("MAIL_WEBHOOK_URL", DEFAULT_WEBHOOK_URL);
        String escapedSubject = escapeJson(subject);
        String escapedBody = escapeJson(body);

        String jsonPayload = "{"
                + "\"to\":\"" + toEmail + "\","
                + "\"subject\":\"" + escapedSubject + "\","
                + "\"body\":\"" + escapedBody + "\","
                + "\"isHTML\":" + isHTML
                + "}";

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        // Google Apps Script trả về 302 chuyển hướng đến trang kết quả JSON
        if (response.statusCode() == 302) {
            String location = response.headers().firstValue("location").orElse(null);
            if (location != null) {
                HttpRequest getReq = HttpRequest.newBuilder()
                        .uri(URI.create(location))
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();
                response = client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            }
        }

        if (response.statusCode() != 200 || !response.body().contains("\"status\":\"success\"")) {
            throw new Exception("Google Apps Script Error (Status " + response.statusCode() + "): " + response.body());
        }
    }

    // =========================================================================
    // 1. GỬI EMAIL KHI ĐỔI MẬT KHẨU THÀNH CÔNG
    // =========================================================================
    public void sendPasswordChangedEmail(String toEmail, String fullName) {
        String displayName = (fullName != null && !fullName.trim().isEmpty()) ? fullName : "Quý khách";
        String timeStr = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        String subject = "[FreshMart] Thông báo cập nhật mật khẩu tài khoản thành công";
        String content = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Thông Báo Đổi Mật Khẩu</title>
            </head>
            <body style="margin: 0; padding: 24px 10px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b; -webkit-font-smoothing: antialiased;">
              <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06); border: 1px solid #e2e8f0;">
                
                <!-- HEADER BRANDING -->
                <tr>
                  <td style="background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%); padding: 32px 24px; text-align: center;">
                    <table cellpadding="0" cellspacing="0" border="0" align="center" style="margin: 0 auto;">
                      <tr>
                        <td style="background-color: #ffffff; color: #059669; font-weight: 900; font-size: 13px; padding: 4px 10px; border-radius: 6px; letter-spacing: 1px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESH</td>
                        <td style="color: #ffffff; font-weight: 800; font-size: 24px; padding-left: 10px; letter-spacing: 0.5px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESHMART ONLINE</td>
                      </tr>
                    </table>
                    <div style="margin: 8px 0 0; font-size: 12.5px; color: #ecfdf5; font-weight: 500; letter-spacing: 0.8px; text-transform: uppercase;">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</div>
                  </td>
                </tr>

                <!-- BODY CONTENT -->
                <tr>
                  <td style="padding: 32px 28px;">
                    <!-- STATUS BADGE -->
                    <div style="text-align: center; margin-bottom: 24px;">
                      <span style="display: inline-block; background-color: #f0fdf4; border: 1px solid #86efac; border-radius: 9999px; padding: 6px 18px; color: #166534; font-size: 13px; font-weight: 700;">
                        <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%%; background-color: #16a34a; margin-right: 8px; vertical-align: middle;"></span>BẢO MẬT TÀI KHOẢN
                      </span>
                    </div>

                    <h2 style="font-size: 18px; font-weight: 700; color: #0f172a; margin: 0 0 14px 0;">Kính chào %s,</h2>
                    <p style="font-size: 15px; line-height: 1.6; color: #334155; margin: 0 0 20px 0;">
                      Hệ thống FreshMart xin thông báo: Mật khẩu đăng nhập cho tài khoản liên kết với hộp thư <strong style="color: #059669;">%s</strong> đã được thay đổi thành công.
                    </p>

                    <!-- INFO CARD -->
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 22px; margin-bottom: 24px;">
                      <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="font-size: 14px;">
                        <tr>
                          <td style="color: #64748b; padding: 6px 0; width: 160px; font-weight: 500;">Thời gian thực hiện:</td>
                          <td style="color: #0f172a; padding: 6px 0; font-weight: 600; text-align: right;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 6px 0; border-top: 1px solid #f1f5f9; font-weight: 500;">Trạng thái:</td>
                          <td style="color: #059669; padding: 6px 0; border-top: 1px solid #f1f5f9; font-weight: 700; text-align: right;">Hoàn tất cập nhật</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 6px 0; border-top: 1px solid #f1f5f9; font-weight: 500;">Thiết bị &amp; Phiên:</td>
                          <td style="color: #0f172a; padding: 6px 0; border-top: 1px solid #f1f5f9; font-weight: 600; text-align: right;">Cổng xác thực Web Portal</td>
                        </tr>
                      </table>
                    </div>

                    <!-- SECURITY WARNING BOX -->
                    <div style="background-color: #fffbeb; border: 1px solid #fde68a; border-left: 4px solid #f59e0b; border-radius: 8px; padding: 16px 20px; margin-bottom: 24px;">
                      <div style="color: #92400e; font-weight: 700; font-size: 14px; margin-bottom: 6px;">[!] BẠN KHÔNG THỰC HIỆN HÀNH ĐỘNG NÀY?</div>
                      <p style="margin: 0; font-size: 13.5px; line-height: 1.6; color: #78350f;">
                        Nếu bạn không thực hiện yêu cầu đổi mật khẩu, rất có thể tài khoản của bạn đang có dấu hiệu xâm nhập bất thường. Vui lòng sử dụng tính năng <strong>Quên mật khẩu</strong> trên website FreshMart ngay lập tức hoặc liên hệ Hotline hỗ trợ khẩn cấp <strong>1900 1234</strong> để khóa tài khoản kịp thời.
                      </p>
                    </div>

                    <p style="margin: 0; font-size: 14px; line-height: 1.6; color: #475569;">
                      Trân trọng cảm ơn quý khách,<br>
                      <strong style="color: #059669; font-size: 14.5px;">Ban Quản Trị Hệ Thống FreshMart Online</strong>
                    </p>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background-color: #f8fafc; padding: 22px 28px; text-align: center; font-size: 12.5px; color: #64748b; border-top: 1px solid #e2e8f0; line-height: 1.6;">
                    <p style="margin: 0 0 4px 0;"><strong style="color: #334155;">Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                    <p style="margin: 0 0 6px 0;">Hotline chăm sóc khách hàng: <strong style="color: #059669;">1900 1234</strong> (7:00 - 21:30 hàng ngày)</p>
                    <p style="margin: 0; font-size: 11.5px; color: #94a3b8;">Email này được gửi tự động từ hệ thống bảo mật, quý khách vui lòng không phản hồi trực tiếp vào địa chỉ này.</p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(displayName, toEmail, timeStr);

        sendEmailAsync(toEmail, subject, content);
    }

    // =========================================================================
    // 2. GỬI EMAIL KHI THANH TOÁN CHUYỂN KHOẢN (PAYOS / VIETQR) THÀNH CÔNG
    // =========================================================================
    public void sendOnlinePaymentSuccessEmail(String toEmail, String fullName, Order order, List<LineItem> lineItems) {
        String displayName = (fullName != null && !fullName.trim().isEmpty()) ? fullName : (order.getTenNguoiNhan() != null ? order.getTenNguoiNhan() : "Quý khách");
        String timeStr = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String orderId = order.getMaDonHang();

        BigDecimal finalTotal = order.getTongTienSauGiamGia() != null && order.getTongTienSauGiamGia().compareTo(BigDecimal.ZERO) > 0
                ? order.getTongTienSauGiamGia()
                : order.getTongTien();

        String formattedTotal = formatVnd(finalTotal);
        String formattedSubtotal = formatVnd(order.getTongTien());
        String formattedDiscount = (order.getTienGiamGia() != null && order.getTienGiamGia().compareTo(BigDecimal.ZERO) > 0)
                ? formatVnd(order.getTienGiamGia()) : "0 đ";

        String itemsHtml = buildLineItemsHtml(lineItems);

        String subject = "[FreshMart] Xác nhận thanh toán trực tuyến thành công - Đơn hàng #" + orderId;
        String content = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Xác Nhận Thanh Toán Trực Tuyến Thành Công</title>
            </head>
            <body style="margin: 0; padding: 24px 10px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b; -webkit-font-smoothing: antialiased;">
              <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width: 640px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06); border: 1px solid #e2e8f0;">
                
                <!-- HEADER BRANDING -->
                <tr>
                  <td style="background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%); padding: 32px 24px; text-align: center;">
                    <table cellpadding="0" cellspacing="0" border="0" align="center" style="margin: 0 auto;">
                      <tr>
                        <td style="background-color: #ffffff; color: #059669; font-weight: 900; font-size: 13px; padding: 4px 10px; border-radius: 6px; letter-spacing: 1px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESH</td>
                        <td style="color: #ffffff; font-weight: 800; font-size: 24px; padding-left: 10px; letter-spacing: 0.5px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESHMART ONLINE</td>
                      </tr>
                    </table>
                    <div style="margin: 8px 0 0; font-size: 12.5px; color: #ecfdf5; font-weight: 500; letter-spacing: 0.8px; text-transform: uppercase;">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</div>
                  </td>
                </tr>

                <!-- BODY CONTENT -->
                <tr>
                  <td style="padding: 32px 28px;">
                    <!-- STATUS BADGE -->
                    <div style="text-align: center; margin-bottom: 24px;">
                      <span style="display: inline-block; background-color: #f0fdf4; border: 1px solid #86efac; border-radius: 9999px; padding: 6px 20px; color: #15803d; font-size: 13.5px; font-weight: 700;">
                        <span style="color: #16a34a; font-weight: 900; margin-right: 6px;">&#10003;</span> ĐÃ THANH TOÁN THÀNH CÔNG (VIETQR / PAYOS)
                      </span>
                    </div>

                    <h2 style="font-size: 18px; font-weight: 700; color: #0f172a; margin: 0 0 12px 0;">Kính chào %s,</h2>
                    <p style="font-size: 15px; line-height: 1.6; color: #334155; margin: 0 0 20px 0;">
                      Cảm ơn quý khách đã tin tưởng mua sắm thực phẩm sạch tại <strong>FreshMart</strong>! Hệ thống đã ghi nhận giao dịch chuyển khoản thành công cho đơn hàng <strong style="color: #059669;">#%s</strong> của quý khách.
                    </p>

                    <!-- ORDER INFO CARD -->
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 22px; margin-bottom: 24px;">
                      <div style="font-size: 14.5px; font-weight: 700; color: #0f172a; margin-bottom: 12px; border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;">
                        THÔNG TIN GIAO NHẬN &amp; ĐƠN HÀNG
                      </div>
                      <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="font-size: 14px;">
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; width: 160px; font-weight: 500;">Mã đơn hàng:</td>
                          <td style="color: #059669; padding: 5px 0; font-weight: 700; font-size: 15px;">#%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Thời gian thanh toán:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Phương thức:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">Chuyển khoản trực tuyến 24/7 (VietQR / PayOS)</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Người nhận hàng:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s (%s)</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Địa chỉ nhận hàng:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s</td>
                        </tr>
                      </table>
                    </div>

                    <!-- PRODUCTS LIST -->
                    <div style="font-size: 15px; font-weight: 700; color: #0f172a; margin: 24px 0 12px 0; border-left: 3px solid #059669; padding-left: 10px;">
                      Chi Tiết Sản Phẩm Đã Mua
                    </div>
                    <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="border-collapse: collapse; margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 10px; overflow: hidden;">
                      <thead>
                        <tr style="background-color: #f1f5f9;">
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: left; border-bottom: 2px solid #cbd5e1;">Tên sản phẩm</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: center; width: 85px; border-bottom: 2px solid #cbd5e1;">SL</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: right; width: 110px; border-bottom: 2px solid #cbd5e1;">Đơn giá</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: right; width: 125px; border-bottom: 2px solid #cbd5e1;">Thành tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>

                    <!-- TOTALS CARD -->
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 22px; margin-bottom: 24px;">
                      <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="font-size: 14.5px;">
                        <tr>
                          <td style="color: #64748b; padding: 5px 0;">Tổng tiền hàng:</td>
                          <td style="text-align: right; font-weight: 600; color: #0f172a; padding: 5px 0;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0;">Voucher / Khuyến mại:</td>
                          <td style="text-align: right; font-weight: 600; color: #ef4444; padding: 5px 0;">-%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0;">Phí vận chuyển:</td>
                          <td style="text-align: right; font-weight: 600; color: #059669; padding: 5px 0;">Miễn phí</td>
                        </tr>
                        <tr>
                          <td style="padding-top: 12px; border-top: 2px dashed #cbd5e1; font-size: 16px; font-weight: 700; color: #0f172a;">TỔNG THANH TOÁN:</td>
                          <td style="padding-top: 12px; border-top: 2px dashed #cbd5e1; text-align: right; font-size: 22px; font-weight: 800; color: #059669;">%s</td>
                        </tr>
                      </table>
                    </div>

                    <!-- SHIPPING NOTICE -->
                    <div style="background-color: #eff6ff; border: 1px solid #bfdbfe; border-left: 4px solid #3b82f6; border-radius: 8px; padding: 14px 18px; margin-bottom: 24px; font-size: 13.5px; color: #1e40af; line-height: 1.5;">
                      Đơn hàng của quý khách đang được đóng gói và bàn giao cho đội ngũ giao nhận FreshMart. Thời gian giao dự kiến từ <strong>1 - 2 giờ</strong> để đảm bảo thực phẩm giữ trọn độ tươi ngon.
                    </div>

                    <p style="margin: 0; font-size: 14px; line-height: 1.6; color: #475569;">
                      Trân trọng cảm ơn quý khách,<br>
                      <strong style="color: #059669; font-size: 14.5px;">Đội Ngũ Phục Vụ FreshMart Online</strong>
                    </p>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background-color: #f8fafc; padding: 22px 28px; text-align: center; font-size: 12.5px; color: #64748b; border-top: 1px solid #e2e8f0; line-height: 1.6;">
                    <p style="margin: 0 0 4px 0;"><strong style="color: #334155;">Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                    <p style="margin: 0 0 6px 0;">Hotline chăm sóc khách hàng: <strong style="color: #059669;">1900 1234</strong> (7:00 - 21:30 hàng ngày)</p>
                    <p style="margin: 0; font-size: 11.5px; color: #94a3b8;">Biên nhận điện tử hợp lệ được xuất tự động từ hệ thống thanh toán FreshMart.</p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                displayName,
                orderId,
                orderId,
                timeStr,
                order.getTenNguoiNhan() != null ? order.getTenNguoiNhan() : displayName,
                order.getSoDienThoaiNhan() != null ? order.getSoDienThoaiNhan() : "---",
                order.getDiaChiGiaoHang() != null ? order.getDiaChiGiaoHang() : "Giao tận nơi",
                itemsHtml,
                formattedSubtotal,
                formattedDiscount,
                formattedTotal
        );

        sendEmailAsync(toEmail, subject, content);
    }

    // =========================================================================
    // 3. GỬI EMAIL KHI NHÂN VIÊN XÁC NHẬN THANH TOÁN TIỀN MẶT (COD)
    // =========================================================================
    public void sendCodPaymentSuccessEmail(String toEmail, String fullName, Order order, List<LineItem> lineItems) {
        String displayName = (fullName != null && !fullName.trim().isEmpty()) ? fullName : (order.getTenNguoiNhan() != null ? order.getTenNguoiNhan() : "Quý khách");
        String timeStr = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String orderId = order.getMaDonHang();

        BigDecimal finalTotal = order.getTongTienSauGiamGia() != null && order.getTongTienSauGiamGia().compareTo(BigDecimal.ZERO) > 0
                ? order.getTongTienSauGiamGia()
                : order.getTongTien();

        String formattedTotal = formatVnd(finalTotal);
        String formattedSubtotal = formatVnd(order.getTongTien());
        String formattedDiscount = (order.getTienGiamGia() != null && order.getTienGiamGia().compareTo(BigDecimal.ZERO) > 0)
                ? formatVnd(order.getTienGiamGia()) : "0 đ";

        String itemsHtml = buildLineItemsHtml(lineItems);

        String subject = "[FreshMart] Biên lai xác nhận thu tiền mặt khi giao hàng (COD) - Đơn hàng #" + orderId;
        String content = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Biên Lai Thu Tiền Mặt COD</title>
            </head>
            <body style="margin: 0; padding: 24px 10px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b; -webkit-font-smoothing: antialiased;">
              <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="max-width: 640px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06); border: 1px solid #e2e8f0;">
                
                <!-- HEADER BRANDING -->
                <tr>
                  <td style="background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%); padding: 32px 24px; text-align: center;">
                    <table cellpadding="0" cellspacing="0" border="0" align="center" style="margin: 0 auto;">
                      <tr>
                        <td style="background-color: #ffffff; color: #059669; font-weight: 900; font-size: 13px; padding: 4px 10px; border-radius: 6px; letter-spacing: 1px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESH</td>
                        <td style="color: #ffffff; font-weight: 800; font-size: 24px; padding-left: 10px; letter-spacing: 0.5px; font-family: 'Segoe UI', Roboto, Arial, sans-serif;">FRESHMART ONLINE</td>
                      </tr>
                    </table>
                    <div style="margin: 8px 0 0; font-size: 12.5px; color: #ecfdf5; font-weight: 500; letter-spacing: 0.8px; text-transform: uppercase;">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</div>
                  </td>
                </tr>

                <!-- BODY CONTENT -->
                <tr>
                  <td style="padding: 32px 28px;">
                    <!-- STATUS BADGE -->
                    <div style="text-align: center; margin-bottom: 24px;">
                      <span style="display: inline-block; background-color: #eff6ff; border: 1px solid #93c5fd; border-radius: 9999px; padding: 6px 20px; color: #1d4ed8; font-size: 13.5px; font-weight: 700;">
                        <span style="color: #2563eb; font-weight: 900; margin-right: 6px;">&#10003;</span> ĐÃ XÁC NHẬN THU TIỀN MẶT (COD)
                      </span>
                    </div>

                    <h2 style="font-size: 18px; font-weight: 700; color: #0f172a; margin: 0 0 12px 0;">Kính chào %s,</h2>
                    <p style="font-size: 15px; line-height: 1.6; color: #334155; margin: 0 0 20px 0;">
                      Nhân viên giao nhận FreshMart vừa xác nhận đã bàn giao đầy đủ hàng hóa và nhận đủ tiền mặt cho đơn hàng <strong style="color: #059669;">#%s</strong> của quý khách.
                    </p>

                    <!-- ORDER INFO CARD -->
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 22px; margin-bottom: 24px;">
                      <div style="font-size: 14.5px; font-weight: 700; color: #0f172a; margin-bottom: 12px; border-bottom: 1px solid #e2e8f0; padding-bottom: 8px;">
                        THÔNG TIN BIÊN LAI THU TIỀN MẶT
                      </div>
                      <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="font-size: 14px;">
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; width: 160px; font-weight: 500;">Mã đơn hàng:</td>
                          <td style="color: #059669; padding: 5px 0; font-weight: 700; font-size: 15px;">#%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Thời gian xác nhận:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Hình thức thanh toán:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">Tiền mặt khi nhận hàng (COD)</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Số tiền đã thu:</td>
                          <td style="color: #059669; padding: 5px 0; font-weight: 700; font-size: 15px;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Người nhận hàng:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s (%s)</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0; font-weight: 500;">Địa chỉ giao hàng:</td>
                          <td style="color: #0f172a; padding: 5px 0; font-weight: 600;">%s</td>
                        </tr>
                      </table>
                    </div>

                    <!-- PRODUCTS LIST -->
                    <div style="font-size: 15px; font-weight: 700; color: #0f172a; margin: 24px 0 12px 0; border-left: 3px solid #059669; padding-left: 10px;">
                      Chi Tiết Sản Phẩm Bàn Giao
                    </div>
                    <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="border-collapse: collapse; margin-bottom: 24px; border: 1px solid #e2e8f0; border-radius: 10px; overflow: hidden;">
                      <thead>
                        <tr style="background-color: #f1f5f9;">
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: left; border-bottom: 2px solid #cbd5e1;">Tên sản phẩm</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: center; width: 85px; border-bottom: 2px solid #cbd5e1;">SL</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: right; width: 110px; border-bottom: 2px solid #cbd5e1;">Đơn giá</th>
                          <th style="padding: 12px 14px; font-size: 13.5px; font-weight: 700; color: #334155; text-align: right; width: 125px; border-bottom: 2px solid #cbd5e1;">Thành tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>

                    <!-- TOTALS CARD -->
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 22px; margin-bottom: 24px;">
                      <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="font-size: 14.5px;">
                        <tr>
                          <td style="color: #64748b; padding: 5px 0;">Tổng tiền hàng:</td>
                          <td style="text-align: right; font-weight: 600; color: #0f172a; padding: 5px 0;">%s</td>
                        </tr>
                        <tr>
                          <td style="color: #64748b; padding: 5px 0;">Voucher / Khuyến mại:</td>
                          <td style="text-align: right; font-weight: 600; color: #ef4444; padding: 5px 0;">-%s</td>
                        </tr>
                        <tr>
                          <td style="padding-top: 12px; border-top: 2px dashed #cbd5e1; font-size: 16px; font-weight: 700; color: #0f172a;">TỔNG TIỀN ĐÃ THU:</td>
                          <td style="padding-top: 12px; border-top: 2px dashed #cbd5e1; text-align: right; font-size: 22px; font-weight: 800; color: #059669;">%s</td>
                        </tr>
                      </table>
                    </div>

                    <p style="margin: 0 0 16px 0; font-size: 14.5px; color: #475569; line-height: 1.6;">
                      Cảm ơn quý khách đã tin tưởng và đồng hành cùng FreshMart. Chúc quý khách và gia đình luôn có những bữa ăn thơm ngon, dinh dưỡng và tràn đầy sức khỏe!
                    </p>
                    <p style="margin: 0; font-size: 14px; line-height: 1.6; color: #475569;">
                      Trân trọng,<br>
                      <strong style="color: #059669; font-size: 14.5px;">Đội Ngũ Phục Vụ FreshMart Online</strong>
                    </p>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="background-color: #f8fafc; padding: 22px 28px; text-align: center; font-size: 12.5px; color: #64748b; border-top: 1px solid #e2e8f0; line-height: 1.6;">
                    <p style="margin: 0 0 4px 0;"><strong style="color: #334155;">Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                    <p style="margin: 0 0 6px 0;">Hotline hỗ trợ &amp; Khiếu nại: <strong style="color: #059669;">1900 1234</strong> | Email: <strong>support@freshmart.vn</strong></p>
                    <p style="margin: 0; font-size: 11.5px; color: #94a3b8;">Biên lai điện tử có giá trị xác nhận đã thanh toán tiền mặt theo quy định của FreshMart.</p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                displayName,
                orderId,
                orderId,
                timeStr,
                formattedTotal,
                order.getTenNguoiNhan() != null ? order.getTenNguoiNhan() : displayName,
                order.getSoDienThoaiNhan() != null ? order.getSoDienThoaiNhan() : "---",
                order.getDiaChiGiaoHang() != null ? order.getDiaChiGiaoHang() : "Giao tận nơi",
                itemsHtml,
                formattedSubtotal,
                formattedDiscount,
                formattedTotal
        );

        sendEmailAsync(toEmail, subject, content);
    }

    private String buildLineItemsHtml(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"text-align: center; color: #94a3b8; padding: 14px;\">Không có chi tiết sản phẩm</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (LineItem item : lineItems) {
            Product prod = item.getProduct();
            String prodName = prod != null ? prod.getTenSanPham() : "Sản phẩm";
            double qty = item.getSoLuong();
            String unit = (prod != null && prod.getDonViTinh() != null) ? prod.getDonViTinh() : "";
            BigDecimal lineTotal = item.getThanhTien();
            BigDecimal unitPrice = (qty > 0 && lineTotal != null)
                    ? lineTotal.divide(BigDecimal.valueOf(qty), 0, java.math.RoundingMode.HALF_UP)
                    : (prod != null && prod.getGiaBan() != null ? prod.getGiaBan() : BigDecimal.ZERO);

            String formattedQty = qty > (long) qty ? String.format("%.2f", qty) : String.valueOf((long) qty);
            String bgColor = (idx % 2 == 1) ? "#fafbfc" : "#ffffff";
            idx++;

            sb.append("<tr style=\"background-color: ").append(bgColor).append(";\">")
              .append("<td style=\"padding: 12px 14px; font-size: 13.5px; border-bottom: 1px solid #f1f5f9; color: #0f172a; font-weight: 600;\">").append(escapeHtml(prodName)).append("</td>")
              .append("<td style=\"padding: 12px 14px; font-size: 13.5px; border-bottom: 1px solid #f1f5f9; text-align: center; color: #475569;\">").append(formattedQty).append(" ").append(escapeHtml(unit)).append("</td>")
              .append("<td style=\"padding: 12px 14px; font-size: 13.5px; border-bottom: 1px solid #f1f5f9; text-align: right; color: #64748b;\">").append(formatVnd(unitPrice)).append("</td>")
              .append("<td style=\"padding: 12px 14px; font-size: 13.5px; border-bottom: 1px solid #f1f5f9; text-align: right; font-weight: 700; color: #0f172a;\">").append(formatVnd(lineTotal)).append("</td>")
              .append("</tr>");
        }
        return sb.toString();
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,d đ", amount.longValue()).replace(',', '.');
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
