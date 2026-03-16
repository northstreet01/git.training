import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner; // 引入键盘监听器

public class GameInit {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/terminal_rpg?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456"; // 确保这里是你刚改的密码

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            Scanner scanner = new Scanner(System.in); // 实例化监听器

            System.out.println("=================================");
            System.out.println("⚔️  欢迎来到终端地下城 V1.0 ⚔️");
            System.out.println("=================================");

            // 1. 从数据库读取玩家和敌人的初始数据 (这里为了简明，直接拉取懒惰的数据)
            ResultSet rsEnemy = stmt.executeQuery("SELECT * FROM enemy WHERE name = '懒惰'");
            rsEnemy.next();
            String enemyName = rsEnemy.getString("name");
            int enemyHp = rsEnemy.getInt("hp");
            int enemyAtk = rsEnemy.getInt("atk");
            rsEnemy.close();

            // 假设我们直接读取了玩家的数据 (暂时硬编码代替，保持代码精简)
            String playerName = "小小北街";
            int playerHp = 50;
            int playerAtk = 15;

            System.out.println("\n突然，一只 [" + enemyName + "] 从黑暗中跳了出来！");

            // 2. 核心战斗循环 (Game Loop)
            while (playerHp > 0 && enemyHp > 0) {
                System.out.println("\n--- 回合开始 ---");
                System.out.println(playerName + " HP: " + playerHp + " | " + enemyName + " HP: " + enemyHp);
                System.out.println("请选择行动：[1] 攻击  [2] 逃跑 [3]喝药 [4]发呆 ");
                System.out.print("👉 你的指令: ");
                
                // 程序挂起，等待你在终端输入数字
                int choice = scanner.nextInt(); 

                if (choice == 1) {
                    // --- 新增：随机数暴击机制 ---
                    int finalDamage = playerAtk;
                    int roll = (int)(Math.random() * 100); // 掷一个 0-99 的骰子
                    
                    if (roll >= 75) { // 15% 概率暴击
                        finalDamage = playerAtk * 2;
                        System.out.println("🔥 暴击！运气爆棚，你对 " + enemyName + " 造成了 " + finalDamage + " 点致命伤害！");
                    } else if (roll <= 20) { // 10% 概率弱击
                        finalDamage = playerAtk / 2;
                        System.out.println("💦 弱击！你的武器滑了一下，仅对 " + enemyName + " 造成了 " + finalDamage + " 点刮痧伤害...");
                    } else {
                        System.out.println("🗡️ 你举起武器，对 " + enemyName + " 造成了 " + finalDamage + " 点伤害！");
                    }
                    
                    enemyHp -= finalDamage;

                    if (enemyHp > 0) {
                        int enemyFinalDamage = enemyAtk;
                        int enemyRoll = (int)(Math.random() * 100);
                        if (enemyRoll >= 80){enemyFinalDamage=enemyAtk*2;}
                        System.out.println("⚠️ 警告！" + enemyName + " 发狂了，触发暴击，对你造成了 " + enemyFinalDamage + " 点巨额伤害！");
                        else if (enemyRoll<=20){enemyFinalDamage=enemyAtk*0.8;
                          System.out.println("💥 " + enemyName + " 愤怒地反击，奈何有点无力，只对你造成了 " + enemyFinalDamage + " 点伤害！");  
                        }
                        else{
                        System.out.println("💥 " + enemyName + " 愤怒地反击，对你造成了 " + enemyFinalDamage + " 点伤害！");
                        playerHp -= enemyFinalDamage;
                    }
                } else if (choice == 2) {
                    System.out.println("🏃‍♂️ 你觉得打不过，撒腿就跑");
                    break; // 打断循环，逃跑成功
                } 
                   else if (choice ==3){
                    System.out.println("🧪 你咕噜咕噜喝下了一瓶红药水，恢复 30 点生命值！");
                     playerHp +=30;
                     if (playerHp > 100) {
                        playerHp = 100;
                    }
                    // 3. 喝药也是要挨打的，补上怪物反击逻辑
                    System.out.println("💥 就在你喝药的间隙，" + enemyName + " 趁机反击，对你造成了 " + enemyAtk + " 点伤害！");
                    playerHp -= enemyAtk;
                   }
                    else {
                    System.out.println("❓ 你的手滑了一下，错失了本回合出手机会！");
                }
            }

            // 3. 战斗结果判定
            System.out.println("\n=================================");
            if (playerHp <= 0) {
                System.out.println("💀 胜败乃兵家常事，大侠重新来过...");
            } else if (enemyHp <= 0) {
                System.out.println("🏆 战斗胜利！你击败了 [" + enemyName + "]！");
                // TODO: 下一步我们要在这里写 UPDATE 语句，把获得的经验值存回数据库
            }
            if (playerHp <= 0) {
                System.out.println("💀 胜败乃兵家常事，大侠重新来过...");
            } else if (enemyHp <= 0) {
                System.out.println("🏆 战斗胜利！你击败了 [" + enemyName + "]！");
                
                // --- 新增：结算战利品并存档 ---
                ResultSet rsLoot = stmt.executeQuery("SELECT exp_reward, gold_reward FROM enemy WHERE name = '" + enemyName + "'");
                if (rsLoot.next()) {
                    int gotExp = rsLoot.getInt("exp_reward");
                    int gotGold = rsLoot.getInt("gold_reward");
                    System.out.println("💰 摸尸体获得：经验 +" + gotExp + " | 金币 +" + gotGold);
                    
                    // 核心动作：写库存档！
                    String updateSql = "UPDATE player SET exp = exp + " + gotExp + ", gold = gold + " + gotGold + " WHERE name = '" + playerName + "'";
                    int rows = stmt.executeUpdate(updateSql);
                    if (rows > 0) {
                        System.out.println("💾 存档成功！数据已写入大冰柜！");
                    }
                }
                rsLoot.close();
            }
            scanner.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}