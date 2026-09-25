package com.saidazi;

import com.saidazi.dto.MatchResultDTO;
import com.saidazi.entity.TeamApplication;
import com.saidazi.entity.Reminder;
import com.saidazi.service.FileService;
import com.saidazi.service.MatchService;
import com.saidazi.service.ReminderService;
import com.saidazi.service.TeamService;
import com.saidazi.task.ReminderScheduledTask;
import com.saidazi.util.MatchAlgorithm;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 四个接口的单元测试
 *
 * 使用 H2 内存数据库，无需安装 MySQL 即可运行
 */
@SpringBootTest
@ActiveProfiles("h2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TeamService teamService;

    // =========================================
    // 测试 ①：赛事定时提醒接口
    // =========================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReminderApiTest {

        @Test
        @Order(1)
        @DisplayName("①-1 设置提醒")
        void testAddReminder() {
            Reminder reminder = reminderService.addReminder(
                    1L, 1L,
                    LocalDateTime.now().plusMinutes(5),
                    1
            );
            assertNotNull(reminder);
            assertEquals(1L, reminder.getUserId());
            assertEquals(0, reminder.getStatus());
            assertEquals(0, reminder.getIsPushed());
            System.out.println("✅ [提醒] 创建成功: id=" + reminder.getId());
        }

        @Test
        @Order(2)
        @DisplayName("①-2 查询用户提醒列表")
        void testGetReminders() {
            var list = reminderService.getUserReminders(1L);
            assertNotNull(list);
            System.out.println("✅ [提醒] 用户1的提醒数: " + list.size());
        }

        @Test
        @Order(3)
        @DisplayName("①-3 定时任务扫描待推送提醒")
        void testProcessPendingReminders() {
            // 测试提醒服务直接扫描
            int count = reminderService.processPendingReminders();
            System.out.println("✅ [定时任务] 返回处理数: " + count);
        }

        @Test
        @Order(4)
        @DisplayName("①-4 取消提醒")
        void testCancelReminder() {
            var list = reminderService.getUserReminders(1L);
            if (!list.isEmpty()) {
                reminderService.cancelReminder(1L, list.get(0).getId());
                System.out.println("✅ [提醒] 取消成功");
            }
        }
    }

    // =========================================
    // 测试 ②：队友智能匹配算法接口
    // =========================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MatchApiTest {

        @Test
        @Order(1)
        @DisplayName("②-1 Jaccard 相似度算法")
        void testJaccardSimilarity() {
            var skills1 = MatchAlgorithm.toSet("前端,后端,算法");
            var skills2 = MatchAlgorithm.toSet("算法,设计,文案");

            double score = MatchAlgorithm.jaccardSimilarity(skills1, skills2);
            System.out.println("✅ [算法] Jaccard 相似度: " + score);

            assertEquals(1.0 / 5.0, score, 0.001);
        }

        @Test
        @Order(2)
        @DisplayName("②-2 智能匹配推荐组队")
        void testRecommendTeams() {
            List<MatchResultDTO> results = matchService.recommendTeams(1L);
            assertNotNull(results);
            System.out.println("✅ [匹配] 推荐组队数: " + results.size());
            for (MatchResultDTO r : results) {
                System.out.println("  └─ " + r.getTitle()
                        + " | 匹配度: " + r.getMatchScore()
                        + " | 共同技能: " + r.getCommonSkills());
            }
        }

        @Test
        @Order(3)
        @DisplayName("②-3 智能匹配推荐队友")
        void testRecommendMates() {
            List<MatchResultDTO> results = matchService.recommendMates(1L);
            assertNotNull(results);
            System.out.println("✅ [匹配] 推荐队友数: " + results.size());
            for (MatchResultDTO r : results) {
                System.out.println("  └─ " + r.getTitle()
                        + " | 匹配度: " + r.getMatchScore()
                        + " | 共同技能: " + r.getCommonSkills());
            }
        }
    }

    // =========================================
    // 测试 ③：图片上传接口
    // =========================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FileUploadApiTest {

        @Test
        @Order(1)
        @DisplayName("③-1 图片上传")
        void testUploadImage() {
            byte[] imageBytes = createMinimalPng();
            String url = fileService.uploadImage(imageBytes, "test.png");
            assertNotNull(url);
            assertTrue(url.startsWith("/uploads/"));
            System.out.println("✅ [上传] 图片URL: " + url);
        }

        @Test
        @Order(2)
        @DisplayName("③-2 非法文件格式拦截")
        void testInvalidFileType() {
            byte[] fakeBytes = "not-an-image".getBytes();
            assertThrows(IllegalArgumentException.class, () -> {
                fileService.uploadImage(fakeBytes, "document.pdf");
            });
            System.out.println("✅ [上传] 非法格式被正确拦截");
        }

        @Test
        @Order(3)
        @DisplayName("③-3 文件格式校验")
        void testIsValidImage() {
            assertTrue(fileService.isValidImage("photo.jpg"));
            assertTrue(fileService.isValidImage("photo.png"));
            assertFalse(fileService.isValidImage("photo.pdf"));
            assertFalse(fileService.isValidImage("photo.exe"));
            System.out.println("✅ [上传] 格式校验正确");
        }

        /** 生成最小合法 PNG 文件 (1x1 灰度像素) */
        private byte[] createMinimalPng() {
            return new byte[]{
                    (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, 0x77,
                    (byte)0x53, (byte)0xDE,
                    0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54,
                    0x08, (byte)0xD7, 0x63, 0x68, 0x60, 0x00, 0x00, 0x00,
                    0x02, 0x00, 0x01, (byte)0xE5, 0x27, (byte)0xDE, (byte)0xFC,
                    0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                    (byte)0xAE, 0x42, 0x60, (byte)0x82
            };
        }
    }

    // =========================================
    // 测试 ④：组队申请审核接口
    // =========================================
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TeamApplicationApiTest {

        @Test
        @Order(1)
        @DisplayName("④-1 申请加入组队（不要求简历）")
        void testApplyTeamNoResume() {
            TeamApplication app = teamService.applyTeam(3L, 1L, null);
            assertNotNull(app);
            assertEquals(0, app.getStatus());
            System.out.println("✅ [申请] 创建成功: id=" + app.getId());
        }

        @Test
        @Order(2)
        @DisplayName("④-2 申请加入组队（要求简历但未传）")
        void testApplyTeamWithResumeRequired() {
            assertThrows(IllegalArgumentException.class, () -> {
                teamService.applyTeam(3L, 2L, null);
            });
            System.out.println("✅ [申请] 要求简历时未传简历被正确拦截");
        }

        @Test
        @Order(3)
        @DisplayName("④-3 通过组队申请")
        void testApproveApplication() {
            var apps = teamService.getReceivedApplications(1L);
            assertFalse(apps.isEmpty());

            TeamApplication app = apps.get(0);
            teamService.processApplication(1L, app.getId(), 1);
            System.out.println("✅ [审核] 申请已通过: id=" + app.getId());
        }

        @Test
        @Order(4)
        @DisplayName("④-4 拒绝组队申请")
        void testRejectApplication() {
            // 让用户2申请组队1（用户2尚未申请过），然后用户1拒绝
            TeamApplication app = teamService.applyTeam(2L, 1L, null);
            teamService.processApplication(1L, app.getId(), 2); // 2=拒绝
            System.out.println("✅ [审核] 申请已拒绝: id=" + app.getId());
        }

        @Test
        @Order(5)
        @DisplayName("④-5 权限校验：非发布人无法审核")
        void testPermissionCheck() {
            var apps = teamService.getReceivedApplications(2L);
            if (!apps.isEmpty()) {
                TeamApplication app = apps.get(0);
                assertThrows(IllegalArgumentException.class, () -> {
                    teamService.processApplication(3L, app.getId(), 1);
                });
                System.out.println("✅ [权限] 非发布人审核被正确拦截");
            }
        }

        @Test
        @Order(6)
        @DisplayName("④-6 重复申请拦截")
        void testDuplicateApply() {
            assertThrows(IllegalArgumentException.class, () -> {
                teamService.applyTeam(3L, 1L, null);
            });
            System.out.println("✅ [防重复] 重复申请被正确拦截");
        }

        @Test
        @Order(7)
        @DisplayName("④-7 获取收到的申请列表")
        void testGetReceivedApplications() {
            var apps = teamService.getReceivedApplications(1L);
            assertNotNull(apps);
            System.out.println("✅ [列表] 用户1收到的申请数: " + apps.size());
        }
    }
}
