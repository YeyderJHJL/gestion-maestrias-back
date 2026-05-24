package com.claudecoders.masters.voucher;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
}
