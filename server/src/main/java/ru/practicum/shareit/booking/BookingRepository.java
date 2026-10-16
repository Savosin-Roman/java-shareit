package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

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

    @Query("""
    SELECT b FROM Booking b
    WHERE b.item.id = :itemId
      AND b.status = 'APPROVED'
      AND b.start < :now
    ORDER BY b.start DESC
    LIMIT 1
""")
    Booking findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("""
    SELECT b FROM Booking b
    WHERE b.item.id = :itemId
      AND b.status = 'APPROVED'
      AND b.start > :now
    ORDER BY b.start ASC
    LIMIT 1
""")
    Booking findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}