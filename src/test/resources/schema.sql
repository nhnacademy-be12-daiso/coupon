DROP TABLE IF EXISTS user_coupons;

CREATE TABLE user_coupons (
                              user_coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_created_id BIGINT NOT NULL,
                              coupon_policy_id BIGINT NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              issue_at TIMESTAMP NOT NULL,
                              expiry_at TIMESTAMP NOT NULL,
                              CONSTRAINT uk_user_policy UNIQUE (user_created_id, coupon_policy_id)
);
