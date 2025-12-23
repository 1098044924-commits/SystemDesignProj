package org.example.accounting.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 交易附件实体（发票等文件）。
 */
@Entity
@Table(name = "transaction_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的交易ID。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /**
     * 文件原始名称。
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * 存储的文件名（UUID）。
     */
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    /**
     * 文件类型（MIME类型）。
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * 文件大小（字节）。
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 上传时间。
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}

