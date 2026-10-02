package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ============================================================
    // Для getAllByBooker — «мои брони» (как арендатора)
    // ============================================================

    @Query("""
        SELECT b FROM Booking b
        WHERE b.booker.id = :bookerId
        ORDER BY b.start DESC
    """)
    List<Booking> findAllByBookerId(@Param("bookerId") Long bookerId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.booker.id = :bookerId
          AND b.status = :status
        ORDER BY b.start DESC
    """)
    List<Booking> findAllByBookerIdAndStatus(
            @Param("bookerId") Long bookerId,
            @Param("status") BookingStatus status);

    // ============================================================
    // Для getAllByOwner — «брони моих вещей» (как владельца)
    // ============================================================

    @Query("""
        SELECT b FROM Booking b
        WHERE b.item.owner.id = :ownerId
        ORDER BY b.start DESC
    """)
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.item.owner.id = :ownerId
          AND b.status = :status
        ORDER BY b.start DESC
    """)
    List<Booking> findAllByItemOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status);

    // ============================================================
    // Пересечения для approve
    // ============================================================

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.item.id = :itemId
          AND b.status = 'APPROVED'
          AND b.id <> :excludeId
          AND b.start < :end
          AND b.end > :start
    """)
    boolean existsApprovedOverlap(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeId);

    // ============================================================
    // Для CommentService — проверка «пользователь арендовал вещь»
    // ============================================================

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.booker.id = :bookerId
          AND b.item.id = :itemId
          AND b.status = 'APPROVED'
          AND b.end < :now
    """)
    boolean existsCompletedBooking(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);
}