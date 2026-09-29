/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : bili_mall

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 29/09/2026 22:04:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
  `goods_id` int NULL DEFAULT NULL COMMENT '商品ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of carousel
-- ----------------------------
INSERT INTO `carousel` VALUES (1, '桑都安1', 'http://localhost:9999/file/download/桑都安1.png', 9);
INSERT INTO `carousel` VALUES (2, '桑都安2', 'http://localhost:9999/file/download/桑都安2.png', 9);
INSERT INTO `carousel` VALUES (3, '桑都安3', 'http://localhost:9999/file/download/桑都安3.png', 9);

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  `goods_id` int NULL DEFAULT NULL COMMENT '商品ID',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collect
-- ----------------------------
INSERT INTO `collect` VALUES (5, 2, 17, '2026-09-26 21:38:42');
INSERT INTO `collect` VALUES (6, 5, 23, '2026-09-27 20:43:31');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详情介绍',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
  `price` double(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `store` int NULL DEFAULT NULL COMMENT '库存',
  `user_id` int NULL DEFAULT NULL COMMENT '添加人',
  `date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上架日期',
  `type_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '上架' COMMENT '状态',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (10, '【松屋】松屋No1 牛めしの具（プレミアム仕様） (1食 (x 30))［冷凍食品］', '', NULL, 'http://localhost:9999/file/download/91D+7U3a4+L._AC_SX679_PIbundle-30,TopRight,0,0_SH20_.jpg', 3980.00, 1, 2, '2026-09-26 20:16:23', 1, '上架', 0, NULL);
INSERT INTO `goods` VALUES (11, 'コカ・コーラ ゼロ ペットボトル 500ml×24本', '', '<p><br></p>', 'http://localhost:9999/file/download/61y6upLBDzL._AC_SL1000_.jpg', 1527.00, 1, 2, '2026-09-26 20:22:18', 2, '上架', 0, NULL);
INSERT INTO `goods` VALUES (13, 'Amazonベーシック ノンフライヤー 4.2L 見やすい窓付き 8種のプリセットメニュー 1200W ブラック', '', '<p><br></p>', 'http://localhost:9999/file/download/718aL23QpRL._AC_SL1500_.jpg', 4500.00, 1, 2, '2026-09-26 21:31:32', 3, '上架', 0, NULL);
INSERT INTO `goods` VALUES (14, '[DawnRain] スポーツウェア レディース ヨガウェア トレーニングウェア ランニングウェア 上下 5点セット 伸縮性 通気 吸汗速乾 フィットネス ジャージ スポーツブラ ロングタイツ パーカ', '', '<p><br></p>', 'http://localhost:9999/file/download/61Cpk9ax0HL._AC_SX679_.jpg', 2600.00, 1, 2, '2026-09-26 21:31:27', 4, '上架', 0, NULL);
INSERT INTO `goods` VALUES (15, 'UVカット 大きい サイズ ビーチ 温泉 水泳 水陸両用 スイミング 通気性 快適 吸汗 速乾柔らかい肌触り おしゃれ プレミアム', '', '<p><br></p>', 'http://localhost:9999/file/download/51FsRrW3IAL._AC_SX679_.jpg', 1980.00, 1, 2, '2026-09-26 21:31:21', 5, '上架', 0, NULL);
INSERT INTO `goods` VALUES (16, '【I\'m home掲載】 ローテーブル 一人暮らし 組立不要 折りたたみ おしゃれ テーブル リビングテーブル 座卓 センターテーブル 机 ちゃぶ台 ちゃぶだい チャブ台 一枚板 (ブラック)', '', '<p><br></p>', 'http://localhost:9999/file/download/81EaIG0knxL._AC_SL1500_.jpg', 3980.00, 1, 2, '2026-09-26 21:31:15', 6, '上架', 0, NULL);
INSERT INTO `goods` VALUES (17, '【特殊5層構造で防サビ加工】 水切りラック 360°排水で水が溜まらず衛生的 [大容量＆省スペース] シンクが広く使える 箸・コップ・お皿もこれ一台ですっきり (ブラック 2段)', '', '<p><br></p>', 'http://localhost:9999/file/download/71Ywa7CtN1L._AC_SL1500_.jpg', 800.00, 0, 5, '2026-09-26 21:32:21', 7, '上架', 0, NULL);
INSERT INTO `goods` VALUES (18, '情報処理教科書 出るとこだけ！基本情報技術者［科目B］テキスト 第5版（効率よく学べると圧倒的人気の定番書！／特典：トレース表／参考書', '', '<p><br></p>', 'http://localhost:9999/file/download/81uR5+u0MTL._SL1500_.jpg', 900.00, 0, 5, '2026-09-26 21:32:12', 8, '上架', 0, NULL);
INSERT INTO `goods` VALUES (19, 'PERFECT DIARY パーフェクトダイアリー【通勤定番ナチュラル】 動物アイシャドウパレット 人気アイメイク 高発色密着 イエベ秋(猫)', '', '<p><br></p>', 'http://localhost:9999/file/download/81iy4JLNDpL._AC_SL1500_.jpg', 2300.00, 0, 5, '2026-09-26 21:32:07', 9, '上架', 0, NULL);
INSERT INTO `goods` VALUES (20, 'Apple iPhone Air 256GB (SIMフリー) 最大120HzのProMotionを採用した6.5インチディスプレイ、パワフルなA19 Proチップ', '', '<p><br></p>', 'http://localhost:9999/file/download/61Ce-6B6x+L._AC_SL1500_.jpg', 120000.00, 1, 5, '2026-09-26 22:18:48', 3, '上架', 0, NULL);
INSERT INTO `goods` VALUES (21, 'コイズミ 扇風機 DCモーター リモコン付き 風量8段階 首振り オン/オフタイマー付き ブラック KLF-3053/K', '', '<p><br></p>', 'http://localhost:9999/file/download/610Hx4-+CtL._AC_SL1500_.jpg', 2000.00, 1, 5, '2026-09-26 22:19:42', 3, '上架', 0, NULL);
INSERT INTO `goods` VALUES (22, 'PELTECH(ペルテック) TDA-712L 電動アシスト e-クロスバイク 27.5インチ シマノ外装7段変速 ', '', '<p><br></p>', 'http://localhost:9999/file/download/61G6qMpL45L._AC_SL1080_.jpg', 60000.00, 1, 5, '2026-09-26 22:20:50', 6, '上架', 0, NULL);
INSERT INTO `goods` VALUES (23, 'Logicool G PRO X SUPERLIGHT 2 ゲーミングマウス G-PPD-004WL-BKd', '', '<p><br></p>', 'http://localhost:9999/file/download/51aHtlvwrGL._AC_SL1500_.jpg', 6000.00, 0, 5, '2026-09-26 22:21:47', 3, '上架', 0, NULL);

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_id` int NULL DEFAULT NULL COMMENT '创建人',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `views` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (8, '【桑都安】簡単の説明', '<p>【プレスリリース】八王子地域密着型C2Cリユースプラットフォーム「桑都安（そうとやす）」が正式リリース！IT技術で学生と街をつなぐ循環型経済を推進</p><p>（東京・八王子） —— 八王子エリアに特化し、個人の不用品リユースや学生間の資源循環を促進するWebプラットフォーム「桑都安 - SOUTOYASU -」が正式にリリースされました。本プラットフォームは「八王子地域密着型 C2Cリユースプラットフォーム（PF）」として、地域の信頼感と利便性を両立した持続可能なリユース体験を提供します。</p><p><br></p><p>1. サービス名「桑都安（そうとやす）」に込めた想い</p><p>古くから養蚕・織物業で栄え、「桑都（そうと）」の美名で親しまれてきた八王子の歴史と誇りを尊重し命名されました。また、「安（やす）」という読みに、ユーザーが心から感じられる「安心・安全な取引」と、学生生活を支える「安い（手頃な価格帯）」という2つの価値を込めています。</p><p><br></p><p>2. 地域特化型として解決する3つの課題</p><p>全国規模のフリマアプリとは一線を画し、地域・学生コミュニティの課題にフォーカスしています。</p><p><br></p><p>送料ゼロの「安心手渡し取引」：</p><p>キャンパス内や八王子市内の主要スポットでの対面受け渡しを推奨。高額な梱包・配送コストを削減するとともに、顔が見える安心感を提供します。</p><p><br></p><p>先輩から後輩への「継承安」（教科書・新生活家電特化）：</p><p>卒業や進级時に廃棄されがちな専門書・IT資格対策本、一人暮らし用家電、自転車などを、新入生・後輩へスムーズに継承する仕組みを構築しました。</p><p><br></p><p>高水準なセキュリティと適正価格：</p><p>地域限定のコミュニティ形成により、トラブルや虚偽出品を防止。安心・安全な取引環境を整備しています。</p><p><br></p><p>3. 今後の展開</p><p>「桑都安」は単なる物品売買システムにとどまらず、IT技術を活用した持続可能な地域社会（SDGs）への貢献を目指しています。八王子の学生と住民をつなぐ温かいコミュニティ基盘として、さらなる機能拡充を進めてまいります。</p>', '2026-09-26 21:50:22', 2, '羅', 11, 1);
INSERT INTO `notice` VALUES (9, '好好好！', '<p><img src=\"http://localhost:9999/file/download/1790686420231_cad9069fc28446ec9a02702ad656516f.webp\" style=\"max-width: 100%; border-radius: 8px; margin-bottom: 12px;\" /></p><p>好好好</p>', '2026-09-29 21:53:47', 2, '羅', 0, 0);

-- ----------------------------
-- Table structure for notice_comment
-- ----------------------------
DROP TABLE IF EXISTS `notice_comment`;
CREATE TABLE `notice_comment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `notice_id` int NOT NULL COMMENT '关联的公告ID',
  `user_id` int NOT NULL COMMENT '评论发布者ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布者昵称/用户名',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布者头像',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice_comment
-- ----------------------------
INSERT INTO `notice_comment` VALUES (1, 8, 5, '123', 'http://localhost:9999/file/download/1790424520313_微信图片_20260926201832_46_29.jpg', 'いいね！', '2026-09-29 21:14:42');

-- ----------------------------
-- Table structure for notice_like
-- ----------------------------
DROP TABLE IF EXISTS `notice_like`;
CREATE TABLE `notice_like`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `notice_id` int NOT NULL COMMENT '帖子/公告ID',
  `user_id` int NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告点赞记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice_like
-- ----------------------------
INSERT INTO `notice_like` VALUES (1, 8, 5, '2026-09-29 21:14:24');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `goods_id` int NULL DEFAULT NULL COMMENT '商品ID',
  `price` double(10, 2) NULL DEFAULT NULL COMMENT '总价',
  `nums` int NULL DEFAULT NULL COMMENT '数量',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `user_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购买时间',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '待付款' COMMENT '订单状态',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (26, '情報処理教科書 出るとこだけ！基本情報技術者［科目B］テキスト 第5版（効率よく学べると圧倒的人気の定番書！／特典：トレース表／参考書', '202609262138035', 18, 900.00, 1, 'tom', '123', '四川', '2026-09-26 21:38:35', '已支付', 2);
INSERT INTO `orders` VALUES (27, 'PERFECT DIARY パーフェクトダイアリー【通勤定番ナチュラル】 動物アイシャドウパレット 人気アイメイク 高発色密着 イエベ秋(猫)', '202609262138046', 19, 2300.00, 1, 'tom', '123', '四川', '2026-09-26 21:38:46', '待支付', 2);
INSERT INTO `orders` VALUES (28, '【特殊5層構造で防サビ加工】 水切りラック 360°排水で水が溜まらず衛生的 [大容量＆省スペース] シンクが広く使える 箸・コップ・お皿もこれ一台ですっきり (ブラック 2段)', '202609262139020', 17, 800.00, 1, 'tom', '123', '四川', '2026-09-26 21:39:20', '待支付', 2);
INSERT INTO `orders` VALUES (29, 'Logicool G PRO X SUPERLIGHT 2 ゲーミングマウス G-PPD-004WL-BKd', '20260929180006864', 23, 6000.00, 1, 'tom', '123', '四川', '2026-09-29 18:00:06', '已支付', 2);

-- ----------------------------
-- Table structure for type
-- ----------------------------
DROP TABLE IF EXISTS `type`;
CREATE TABLE `type`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of type
-- ----------------------------
INSERT INTO `type` VALUES (1, '食品');
INSERT INTO `type` VALUES (2, '飲料・ドリンク');
INSERT INTO `type` VALUES (3, '家電・スマホ');
INSERT INTO `type` VALUES (4, 'レディース');
INSERT INTO `type` VALUES (5, 'メンズ');
INSERT INTO `type` VALUES (6, '家具');
INSERT INTO `type` VALUES (7, '日用品');
INSERT INTO `type` VALUES (8, '本・教科書');
INSERT INTO `type` VALUES (9, 'コスメ・美容');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `infos` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `accoun` double(10, 2) NULL DEFAULT 0.00 COMMENT '余额',
  `account` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123', '管理员', '123', 'admin@xxx.com', '四川', 'http://localhost:9999/file/download/微信图片_20260926201832_46_29.jpg', '男', 27, '我是管理员', 'ADMIN', 0.00, 0.00);
INSERT INTO `user` VALUES (2, 'tom', '123', '羅', '123', 'jerry@qq.com', '四川', 'http://localhost:9999/file/download/1790425207008_微信图片_20260926201832_46_29.jpg', '男', 28, 'nice！', 'USER', 0.00, 4016.00);
INSERT INTO `user` VALUES (3, 'jerry', '123', '杰瑞', '15098765321', 'tom@qq.com', '上海', 'http://localhost:9999/file/download/杰瑞.jpg', '男', 25, '1234', 'USER', 0.00, 0.00);
INSERT INTO `user` VALUES (4, 'mianbao', '123', 'mianbao', NULL, NULL, NULL, 'http://localhost:9999/file/download/1790424543399_微信图片_20260409115315_176_20.jpg', NULL, NULL, NULL, 'USER', 0.00, 0.00);
INSERT INTO `user` VALUES (5, '123', '123', '123', NULL, NULL, NULL, 'http://localhost:9999/file/download/1790424520313_微信图片_20260926201832_46_29.jpg', NULL, NULL, NULL, 'USER', 0.00, 6000.00);
INSERT INTO `user` VALUES (6, '666', '666', '666', NULL, NULL, NULL, 'pixel:1949944393', NULL, NULL, NULL, 'USER', 0.00, 0.00);

SET FOREIGN_KEY_CHECKS = 1;
