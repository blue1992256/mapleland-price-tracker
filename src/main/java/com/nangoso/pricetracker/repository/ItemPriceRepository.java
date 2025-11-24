package com.nangoso.pricetracker.repository;

import com.nangoso.pricetracker.entity.Item;
import com.nangoso.pricetracker.entity.ItemPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemPriceRepository extends JpaRepository<ItemPrice, Long> {

    List<ItemPrice> findByItemAndDateOrderByDateDesc(Item item, LocalDate date);

    List<ItemPrice> findByItemOrderByDateDesc(Item item);

    @Query("SELECT ip FROM ItemPrice ip WHERE ip.item = :item AND ip.date >= :startDate ORDER BY ip.date DESC")
    List<ItemPrice> findByItemAndDateAfterOrderByDateDesc(@Param("item") Item item, @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(ip) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date")
    Long countByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜의 ACTIVE 상태 가격 리스트 조회
     */
    @Query("SELECT ip FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date AND ip.status = 'ACTIVE' ORDER BY ip.price ASC")
    List<ItemPrice> findActiveByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜의 모든 가격 리스트 조회 (상태 무관)
     */
    @Query("SELECT ip FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date ORDER BY ip.price ASC")
    List<ItemPrice> findAllByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜에 해당 URL이 존재하는지 확인
     */
    boolean existsByItemAndDateAndUrl(Item item, LocalDate date, String url);

    /**
     * 특정 아이템의 최신 날짜 평균 가격 조회
     */
    @Query("SELECT AVG(ip.price) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = (SELECT MAX(ip2.date) FROM ItemPrice ip2 WHERE ip2.item = :item)")
    Double findLatestAveragePriceByItem(@Param("item") Item item);

    /**
     * 특정 아이템의 특정 날짜 최소 가격 조회 (ACTIVE 상태만)
     */
    @Query("SELECT MIN(ip.price) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date AND ip.status = 'ACTIVE'")
    Long findMinPriceByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜 최대 가격 조회 (ACTIVE 상태만)
     */
    @Query("SELECT MAX(ip.price) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date AND ip.status = 'ACTIVE'")
    Long findMaxPriceByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜 평균 가격 조회 (ACTIVE 상태만)
     */
    @Query("SELECT AVG(ip.price) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date AND ip.status = 'ACTIVE'")
    Double findAvgPriceByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 특정 날짜 거래 건수 조회 (ACTIVE 상태만)
     */
    @Query("SELECT COUNT(ip) FROM ItemPrice ip WHERE ip.item = :item AND ip.date = :date AND ip.status = 'ACTIVE'")
    Integer findCountByItemAndDate(@Param("item") Item item, @Param("date") LocalDate date);

    /**
     * 특정 아이템의 날짜별 가격 데이터 조회 (최근 n일, ACTIVE 상태만)
     */
    @Query("SELECT ip.date as date, AVG(ip.price) as avgPrice, MIN(ip.price) as minPrice, MAX(ip.price) as maxPrice, COUNT(ip) as count " +
           "FROM ItemPrice ip WHERE ip.item = :item AND ip.date >= :startDate AND ip.status = 'ACTIVE' " +
           "GROUP BY ip.date ORDER BY ip.date DESC")
    List<Object[]> findDailyStatsByItemAndDateAfter(@Param("item") Item item, @Param("startDate") LocalDate startDate);
}
