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
 * Cơ chế gửi thuần Webhook (bỏ hoàn toàn Fallback SMTP).
 * Hỗ trợ gửi bất đồng bộ (Asynchronous) không gây nghẽn HTTP thread.
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
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Google Apps Script trả về 302 chuyển hướng đến trang kết quả JSON
        if (response.statusCode() == 302) {
            String location = response.headers().firstValue("location").orElse(null);
            if (location != null) {
                HttpRequest getReq = HttpRequest.newBuilder()
                        .uri(URI.create(location))
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();
                response = client.send(getReq, HttpResponse.BodyHandlers.ofString());
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
              <style>
                body {
                  margin: 0;
                  padding: 30px 10px;
                  background-color: #f1f5f9;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                  color: #1e293b;
                  -webkit-font-smoothing: antialiased;
                }
                .container {
                  max-width: 620px;
                  margin: 0 auto;
                  background-color: #ffffff;
                  border-radius: 16px;
                  overflow: hidden;
                  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
                  border: 1px solid #e2e8f0;
                }
                .header {
                  background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%);
                  padding: 36px 28px;
                  text-align: center;
                  color: #ffffff;
                }
                .header .logo {
                  font-size: 26px;
                  font-weight: 800;
                  letter-spacing: 1px;
                  margin: 0;
                  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .header .tagline {
                  margin: 6px 0 0;
                  font-size: 13.5px;
                  color: #ecfdf5;
                  font-weight: 500;
                  letter-spacing: 0.5px;
                  text-transform: uppercase;
                }
                .body {
                  padding: 36px 32px;
                }
                .badge-wrapper {
                  text-align: center;
                  margin-bottom: 24px;
                }
                .status-badge {
                  display: inline-block;
                  background-color: #f0fdf4;
                  color: #166534;
                  border: 1px solid #bbf7d0;
                  font-weight: 700;
                  padding: 6px 18px;
                  border-radius: 9999px;
                  font-size: 13px;
                }
                .greeting {
                  font-size: 18px;
                  font-weight: 700;
                  color: #0f172a;
                  margin: 0 0 14px 0;
                }
                .intro-text {
                  font-size: 15px;
                  line-height: 1.65;
                  color: #334155;
                  margin: 0 0 24px 0;
                }
                .info-card {
                  background-color: #f8fafc;
                  border: 1px solid #e2e8f0;
                  border-radius: 12px;
                  padding: 20px 22px;
                  margin-bottom: 24px;
                }
                .info-row {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  padding: 8px 0;
                  border-bottom: 1px solid #f1f5f9;
                  font-size: 14px;
                }
                .info-row:last-child {
                  border-bottom: none;
                  padding-bottom: 0;
                }
                .info-row:first-child {
                  padding-top: 0;
                }
                .info-label {
                  color: #64748b;
                  font-weight: 500;
                }
                .info-value {
                  color: #0f172a;
                  font-weight: 600;
                }
                .warning-box {
                  background-color: #fffbeb;
                  border: 1px solid #fde68a;
                  border-left: 4px solid #f59e0b;
                  border-radius: 8px;
                  padding: 16px 20px;
                  margin-bottom: 28px;
                }
                .warning-box strong {
                  display: block;
                  color: #92400e;
                  font-size: 14.5px;
                  margin-bottom: 6px;
                }
                .warning-box p {
                  margin: 0;
                  font-size: 13.5px;
                  line-height: 1.6;
                  color: #78350f;
                }
                .footer {
                  background-color: #f8fafc;
                  padding: 24px 32px;
                  text-align: center;
                  font-size: 13px;
                  color: #64748b;
                  border-top: 1px solid #e2e8f0;
                  line-height: 1.6;
                }
                .footer strong {
                  color: #334155;
                }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1 class="logo">🌿 FRESHMART ONLINE</h1>
                  <p class="tagline">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</p>
                </div>
                <div class="body">
                  <div class="badge-wrapper">
                    <span class="status-badge">🔒 BẢO MẬT TÀI KHOẢN</span>
                  </div>

                  <p class="greeting">Kính chào %s,</p>
                  <p class="intro-text">
                    Hệ thống FreshMart xin thông báo: Mật khẩu đăng nhập cho tài khoản liên kết với hộp thư <strong>%s</strong> đã được thay đổi thành công.
                  </p>

                  <div class="info-card">
                    <div class="info-row">
                      <span class="info-label">Thời gian thực hiện:</span>
                      <span class="info-value">%s</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">Trạng thái:</span>
                      <span class="info-value" style="color: #059669;">Hoàn tất cập nhật</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">Thiết bị &amp; Phiên:</span>
                      <span class="info-value">Cổng xác thực Web Portal</span>
                    </div>
                  </div>

                  <div class="warning-box">
                    <strong>⚠️ Bạn không thực hiện hành động này?</strong>
                    <p>
                      Nếu bạn không thực hiện yêu cầu đổi mật khẩu, rất có thể tài khoản của bạn đang có dấu hiệu xâm nhập bất thường. Vui lòng bấm "Quên mật khẩu" trên ứng dụng FreshMart ngay lập tức hoặc liên hệ Hotline hỗ trợ khẩn cấp <strong>1900 1234</strong>.
                    </p>
                  </div>

                  <p style="margin: 0; font-size: 14.5px; color: #475569;">
                    Trân trọng cảm ơn quý khách,<br>
                    <strong style="color: #059669;">Ban Quản Trị Hệ Thống FreshMart</strong>
                  </p>
                </div>

                <div class="footer">
                  <p style="margin: 0 0 6px 0;"><strong>Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                  <p style="margin: 0 0 6px 0;">Hotline chăm sóc khách hàng: <strong>1900 1234</strong> (7:00 - 21:30 hàng ngày)</p>
                  <p style="margin: 0; font-size: 12px; color: #94a3b8;">Email này được gửi tự động từ hệ thống bảo mật, quý khách vui lòng không phản hồi trực tiếp vào địa chỉ này.</p>
                </div>
              </div>
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
              <style>
                body {
                  margin: 0;
                  padding: 30px 10px;
                  background-color: #f1f5f9;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                  color: #1e293b;
                  -webkit-font-smoothing: antialiased;
                }
                .container {
                  max-width: 660px;
                  margin: 0 auto;
                  background-color: #ffffff;
                  border-radius: 16px;
                  overflow: hidden;
                  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
                  border: 1px solid #e2e8f0;
                }
                .header {
                  background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%);
                  padding: 36px 28px;
                  text-align: center;
                  color: #ffffff;
                }
                .header .logo {
                  font-size: 26px;
                  font-weight: 800;
                  letter-spacing: 1px;
                  margin: 0;
                }
                .header .tagline {
                  margin: 6px 0 0;
                  font-size: 13.5px;
                  color: #ecfdf5;
                  font-weight: 500;
                  letter-spacing: 0.5px;
                  text-transform: uppercase;
                }
                .body {
                  padding: 36px 32px;
                }
                .badge-wrapper {
                  text-align: center;
                  margin-bottom: 24px;
                }
                .status-badge {
                  display: inline-block;
                  background-color: #f0fdf4;
                  color: #15803d;
                  border: 1px solid #86efac;
                  font-weight: 700;
                  padding: 6px 20px;
                  border-radius: 9999px;
                  font-size: 13.5px;
                }
                .greeting {
                  font-size: 18px;
                  font-weight: 700;
                  color: #0f172a;
                  margin: 0 0 12px 0;
                }
                .intro-text {
                  font-size: 15px;
                  line-height: 1.65;
                  color: #334155;
                  margin: 0 0 24px 0;
                }
                .info-card {
                  background-color: #f8fafc;
                  border: 1px solid #e2e8f0;
                  border-radius: 12px;
                  padding: 20px 22px;
                  margin-bottom: 28px;
                }
                .info-card h4 {
                  margin: 0 0 14px 0;
                  font-size: 15px;
                  color: #0f172a;
                  font-weight: 700;
                  border-bottom: 1px solid #e2e8f0;
                  padding-bottom: 8px;
                }
                .info-table {
                  width: 100%%;
                  border-collapse: collapse;
                  font-size: 14px;
                }
                .info-table td {
                  padding: 6px 0;
                  vertical-align: top;
                }
                .info-table td.label {
                  color: #64748b;
                  width: 160px;
                  font-weight: 500;
                }
                .info-table td.val {
                  color: #0f172a;
                  font-weight: 600;
                }
                .section-title {
                  font-size: 16px;
                  font-weight: 700;
                  color: #0f172a;
                  margin: 28px 0 12px 0;
                  display: flex;
                  align-items: center;
                  gap: 8px;
                }
                .items-table {
                  width: 100%%;
                  border-collapse: collapse;
                  margin-bottom: 24px;
                  border: 1px solid #e2e8f0;
                  border-radius: 10px;
                  overflow: hidden;
                }
                .items-table th {
                  background-color: #f1f5f9;
                  color: #334155;
                  font-size: 13.5px;
                  font-weight: 700;
                  padding: 12px 14px;
                  text-align: left;
                  border-bottom: 2px solid #cbd5e1;
                }
                .items-table td {
                  padding: 13px 14px;
                  font-size: 14px;
                  border-bottom: 1px solid #f1f5f9;
                  color: #1e293b;
                }
                .items-table tr:last-child td {
                  border-bottom: none;
                }
                .items-table tr:nth-child(even) {
                  background-color: #fafbfc;
                }
                .total-card {
                  background-color: #f8fafc;
                  border: 1px solid #e2e8f0;
                  border-radius: 12px;
                  padding: 20px 24px;
                  margin-bottom: 28px;
                }
                .total-table {
                  width: 100%%;
                  border-collapse: collapse;
                  font-size: 14.5px;
                }
                .total-table td {
                  padding: 6px 0;
                }
                .total-table td.t-label {
                  color: #64748b;
                }
                .total-table td.t-val {
                  text-align: right;
                  font-weight: 600;
                  color: #0f172a;
                }
                .total-table tr.final-row td {
                  padding-top: 14px;
                  border-top: 2px dashed #cbd5e1;
                }
                .final-amount {
                  font-size: 22px;
                  font-weight: 800;
                  color: #059669;
                }
                .shipping-notice {
                  background-color: #eff6ff;
                  border: 1px solid #bfdbfe;
                  border-left: 4px solid #3b82f6;
                  border-radius: 8px;
                  padding: 14px 18px;
                  margin-bottom: 28px;
                  font-size: 14px;
                  color: #1e40af;
                  line-height: 1.5;
                }
                .footer {
                  background-color: #f8fafc;
                  padding: 24px 32px;
                  text-align: center;
                  font-size: 13px;
                  color: #64748b;
                  border-top: 1px solid #e2e8f0;
                  line-height: 1.6;
                }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1 class="logo">🌿 FRESHMART ONLINE</h1>
                  <p class="tagline">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</p>
                </div>
                <div class="body">
                  <div class="badge-wrapper">
                    <span class="status-badge">✓ ĐÃ THANH TOÁN THÀNH CÔNG (VIETQR / PAYOS)</span>
                  </div>

                  <p class="greeting">Kính chào %s,</p>
                  <p class="intro-text">
                    Cảm ơn quý khách đã tin tưởng mua sắm thực phẩm sạch tại <strong>FreshMart</strong>! Hệ thống đã ghi nhận giao dịch chuyển khoản thành công cho đơn hàng <strong>#%s</strong> của quý khách.
                  </p>

                  <div class="info-card">
                    <h4>📍 Thông Tin Giao Nhận &amp; Đơn Hàng</h4>
                    <table class="info-table">
                      <tr>
                        <td class="label">Mã đơn hàng:</td>
                        <td class="val" style="color: #059669; font-weight: 700; font-size: 15px;">#%s</td>
                      </tr>
                      <tr>
                        <td class="label">Thời gian thanh toán:</td>
                        <td class="val">%s</td>
                      </tr>
                      <tr>
                        <td class="label">Phương thức:</td>
                        <td class="val">Chuyển khoản trực tuyến 24/7 (VietQR / PayOS)</td>
                      </tr>
                      <tr>
                        <td class="label">Người nhận hàng:</td>
                        <td class="val">%s (%s)</td>
                      </tr>
                      <tr>
                        <td class="label">Địa chỉ nhận hàng:</td>
                        <td class="val">%s</td>
                      </tr>
                    </table>
                  </div>

                  <div class="section-title">🛒 Chi Tiết Sản Phẩm Đã Mua</div>
                  <table class="items-table">
                    <thead>
                      <tr>
                        <th>Tên sản phẩm</th>
                        <th style="text-align: center; width: 85px;">SL</th>
                        <th style="text-align: right; width: 110px;">Đơn giá</th>
                        <th style="text-align: right; width: 125px;">Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>

                  <div class="total-card">
                    <table class="total-table">
                      <tr>
                        <td class="t-label">Tổng tiền hàng:</td>
                        <td class="t-val">%s</td>
                      </tr>
                      <tr>
                        <td class="t-label">Voucher / Khuyến mại:</td>
                        <td class="t-val" style="color: #ef4444;">-%s</td>
                      </tr>
                      <tr>
                        <td class="t-label">Phí vận chuyển:</td>
                        <td class="t-val" style="color: #059669;">Miễn phí</td>
                      </tr>
                      <tr class="final-row">
                        <td style="font-size: 16px; font-weight: 700; color: #0f172a;">TỔNG THANH TOÁN:</td>
                        <td class="t-val"><span class="final-amount">%s</span></td>
                      </tr>
                    </table>
                  </div>

                  <div class="shipping-notice">
                    🚚 <strong>Đóng gói &amp; Vận chuyển:</strong> Đơn hàng đang được nhân viên FreshMart tuyển chọn các thực phẩm tươi mới nhất, đóng gói giữ lạnh đạt chuẩn và giao tới bạn trong khung giờ sớm nhất.
                  </div>

                  <p style="margin: 0; font-size: 14.5px; color: #475569;">
                    Trân trọng cảm ơn quý khách,<br>
                    <strong style="color: #059669;">Đội Ngũ Phục Vụ FreshMart Online</strong>
                  </p>
                </div>

                <div class="footer">
                  <p style="margin: 0 0 6px 0;"><strong>Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                  <p style="margin: 0 0 6px 0;">Hotline hỗ trợ: <strong>1900 1234</strong> | Email: <strong>support@freshmart.vn</strong></p>
                  <p style="margin: 0; font-size: 12px; color: #94a3b8;">Hóa đơn này được tạo tự động từ hệ thống điện tử FreshMart.</p>
                </div>
              </div>
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
              <style>
                body {
                  margin: 0;
                  padding: 30px 10px;
                  background-color: #f1f5f9;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                  color: #1e293b;
                  -webkit-font-smoothing: antialiased;
                }
                .container {
                  max-width: 660px;
                  margin: 0 auto;
                  background-color: #ffffff;
                  border-radius: 16px;
                  overflow: hidden;
                  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
                  border: 1px solid #e2e8f0;
                }
                .header {
                  background: linear-gradient(135deg, #064e3b 0%%, #059669 60%%, #10b981 100%%);
                  padding: 36px 28px;
                  text-align: center;
                  color: #ffffff;
                }
                .header .logo {
                  font-size: 26px;
                  font-weight: 800;
                  letter-spacing: 1px;
                  margin: 0;
                }
                .header .tagline {
                  margin: 6px 0 0;
                  font-size: 13.5px;
                  color: #ecfdf5;
                  font-weight: 500;
                  letter-spacing: 0.5px;
                  text-transform: uppercase;
                }
                .body {
                  padding: 36px 32px;
                }
                .badge-wrapper {
                  text-align: center;
                  margin-bottom: 24px;
                }
                .status-badge {
                  display: inline-block;
                  background-color: #eff6ff;
                  color: #1d4ed8;
                  border: 1px solid #93c5fd;
                  font-weight: 700;
                  padding: 6px 20px;
                  border-radius: 9999px;
                  font-size: 13.5px;
                }
                .greeting {
                  font-size: 18px;
                  font-weight: 700;
                  color: #0f172a;
                  margin: 0 0 12px 0;
                }
                .intro-text {
                  font-size: 15px;
                  line-height: 1.65;
                  color: #334155;
                  margin: 0 0 24px 0;
                }
                .info-card {
                  background-color: #f8fafc;
                  border: 1px solid #e2e8f0;
                  border-radius: 12px;
                  padding: 20px 22px;
                  margin-bottom: 28px;
                }
                .info-card h4 {
                  margin: 0 0 14px 0;
                  font-size: 15px;
                  color: #0f172a;
                  font-weight: 700;
                  border-bottom: 1px solid #e2e8f0;
                  padding-bottom: 8px;
                }
                .info-table {
                  width: 100%%;
                  border-collapse: collapse;
                  font-size: 14px;
                }
                .info-table td {
                  padding: 6px 0;
                  vertical-align: top;
                }
                .info-table td.label {
                  color: #64748b;
                  width: 160px;
                  font-weight: 500;
                }
                .info-table td.val {
                  color: #0f172a;
                  font-weight: 600;
                }
                .section-title {
                  font-size: 16px;
                  font-weight: 700;
                  color: #0f172a;
                  margin: 28px 0 12px 0;
                  display: flex;
                  align-items: center;
                  gap: 8px;
                }
                .items-table {
                  width: 100%%;
                  border-collapse: collapse;
                  margin-bottom: 24px;
                  border: 1px solid #e2e8f0;
                  border-radius: 10px;
                  overflow: hidden;
                }
                .items-table th {
                  background-color: #f1f5f9;
                  color: #334155;
                  font-size: 13.5px;
                  font-weight: 700;
                  padding: 12px 14px;
                  text-align: left;
                  border-bottom: 2px solid #cbd5e1;
                }
                .items-table td {
                  padding: 13px 14px;
                  font-size: 14px;
                  border-bottom: 1px solid #f1f5f9;
                  color: #1e293b;
                }
                .items-table tr:last-child td {
                  border-bottom: none;
                }
                .items-table tr:nth-child(even) {
                  background-color: #fafbfc;
                }
                .total-card {
                  background-color: #f8fafc;
                  border: 1px solid #e2e8f0;
                  border-radius: 12px;
                  padding: 20px 24px;
                  margin-bottom: 28px;
                }
                .total-table {
                  width: 100%%;
                  border-collapse: collapse;
                  font-size: 14.5px;
                }
                .total-table td {
                  padding: 6px 0;
                }
                .total-table td.t-label {
                  color: #64748b;
                }
                .total-table td.t-val {
                  text-align: right;
                  font-weight: 600;
                  color: #0f172a;
                }
                .total-table tr.final-row td {
                  padding-top: 14px;
                  border-top: 2px dashed #cbd5e1;
                }
                .final-amount {
                  font-size: 22px;
                  font-weight: 800;
                  color: #059669;
                }
                .footer {
                  background-color: #f8fafc;
                  padding: 24px 32px;
                  text-align: center;
                  font-size: 13px;
                  color: #64748b;
                  border-top: 1px solid #e2e8f0;
                  line-height: 1.6;
                }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1 class="logo">🌿 FRESHMART ONLINE</h1>
                  <p class="tagline">Hệ Thống Phân Phối Nông Sản &amp; Thực Phẩm Tươi Sạch</p>
                </div>
                <div class="body">
                  <div class="badge-wrapper">
                    <span class="status-badge">💵 ĐÃ THU ĐỦ TIỀN MẶT (COD)</span>
                  </div>

                  <p class="greeting">Kính chào %s,</p>
                  <p class="intro-text">
                    Nhân viên giao nhận FreshMart vừa xác nhận đã bàn giao đầy đủ hàng hóa và nhận đủ tiền mặt cho đơn hàng <strong>#%s</strong> của quý khách.
                  </p>

                  <div class="info-card">
                    <h4>📍 Thông Tin Biên Lai Thu Tiền Mặt</h4>
                    <table class="info-table">
                      <tr>
                        <td class="label">Mã đơn hàng:</td>
                        <td class="val" style="color: #059669; font-weight: 700; font-size: 15px;">#%s</td>
                      </tr>
                      <tr>
                        <td class="label">Thời gian xác nhận:</td>
                        <td class="val">%s</td>
                      </tr>
                      <tr>
                        <td class="label">Hình thức thanh toán:</td>
                        <td class="val">Tiền mặt khi nhận hàng (COD)</td>
                      </tr>
                      <tr>
                        <td class="label">Số tiền đã thu:</td>
                        <td class="val" style="color: #059669; font-size: 16px;">%s</td>
                      </tr>
                      <tr>
                        <td class="label">Người nhận hàng:</td>
                        <td class="val">%s (%s)</td>
                      </tr>
                      <tr>
                        <td class="label">Địa chỉ giao hàng:</td>
                        <td class="val">%s</td>
                      </tr>
                    </table>
                  </div>

                  <div class="section-title">🛒 Chi Tiết Sản Phẩm Bàn Giao</div>
                  <table class="items-table">
                    <thead>
                      <tr>
                        <th>Tên sản phẩm</th>
                        <th style="text-align: center; width: 85px;">SL</th>
                        <th style="text-align: right; width: 110px;">Đơn giá</th>
                        <th style="text-align: right; width: 125px;">Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>

                  <div class="total-card">
                    <table class="total-table">
                      <tr>
                        <td class="t-label">Tổng tiền hàng:</td>
                        <td class="t-val">%s</td>
                      </tr>
                      <tr>
                        <td class="t-label">Voucher / Khuyến mại:</td>
                        <td class="t-val" style="color: #ef4444;">-%s</td>
                      </tr>
                      <tr class="final-row">
                        <td style="font-size: 16px; font-weight: 700; color: #0f172a;">TỔNG TIỀN ĐÃ THU:</td>
                        <td class="t-val"><span class="final-amount">%s</span></td>
                      </tr>
                    </table>
                  </div>

                  <p style="margin: 0 0 16px 0; font-size: 14.5px; color: #475569; line-height: 1.6;">
                    Cảm ơn quý khách đã tin tưởng và đồng hành cùng FreshMart. Chúc quý khách và gia đình luôn có những bữa ăn thơm ngon, dinh dưỡng và tràn đầy sức khỏe!
                  </p>
                  <p style="margin: 0; font-size: 14.5px; color: #475569;">
                    Trân trọng,<br>
                    <strong style="color: #059669;">Đội Ngũ Phục Vụ FreshMart Online</strong>
                  </p>
                </div>

                <div class="footer">
                  <p style="margin: 0 0 6px 0;"><strong>Công Ty Cổ Phần Bách Hóa Xanh Trực Tuyến - FreshMart</strong></p>
                  <p style="margin: 0 0 6px 0;">Hotline hỗ trợ &amp; Khiếu nại: <strong>1900 1234</strong> | Email: <strong>support@freshmart.vn</strong></p>
                  <p style="margin: 0; font-size: 12px; color: #94a3b8;">Biên lai điện tử có giá trị xác nhận đã thanh toán tiền mặt theo quy định của FreshMart.</p>
                </div>
              </div>
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

            sb.append("<tr>")
              .append("<td><strong style=\"color: #0f172a;\">").append(escapeHtml(prodName)).append("</strong></td>")
              .append("<td style=\"text-align: center; color: #475569;\">").append(formattedQty).append(" ").append(escapeHtml(unit)).append("</td>")
              .append("<td style=\"text-align: right; color: #64748b;\">").append(formatVnd(unitPrice)).append("</td>")
              .append("<td style=\"text-align: right; font-weight: 700; color: #0f172a;\">").append(formatVnd(lineTotal)).append("</td>")
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
