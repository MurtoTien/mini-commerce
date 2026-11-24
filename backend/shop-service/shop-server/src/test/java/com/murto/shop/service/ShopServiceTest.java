package com.murto.shop.service;

import com.murto.shop.api.dto.ShopInfo;
import com.murto.shop.dao.ShopDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {

    private final RecordingShopDAO dao = new RecordingShopDAO();
    private final ShopService service = new ShopService(dao);

    @Test
    void rejectsBlankNameBeforeUpdating() {
        ShopInfo request = new ShopInfo(1L, "  ", null, "PERSONAL", null);

        assertThrows(IllegalArgumentException.class, () -> service.updateShop(request));
        assertEquals(0, dao.writeCalls);
    }

    @Test
    void rejectsInvalidIdsBeforeWriting() {
        ShopInfo request = new ShopInfo(0L, "店铺", null, "PERSONAL", null);

        assertThrows(IllegalArgumentException.class, () -> service.updateShop(request));
        assertThrows(IllegalArgumentException.class, () -> service.deleteShop(null));
        assertThrows(IllegalArgumentException.class, () -> service.deleteShop(-1L));
        assertEquals(0, dao.writeCalls);
    }

    @Test
    void reportsMissingShopInsteadOfReturningNull() {
        ShopInfo request = new ShopInfo(1L, "店铺", null, "PERSONAL", null);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> service.updateShop(request));

        assertEquals("店铺不存在", error.getMessage());
    }

    @Test
    void returnsPersistedRecordRatherThanCallerSuppliedCreationTime() {
        ShopInfo request = new ShopInfo(1L, "新名称", null, "PERSONAL",
                LocalDateTime.of(2030, 1, 1, 0, 0));
        dao.updateResult = new ShopInfo(1L, "新名称", null, "PERSONAL",
                LocalDateTime.of(2026, 1, 1, 0, 0));

        assertEquals(dao.updateResult, service.updateShop(request));
    }

    @Test
    void returnsFalseWhenDeleteDoesNotRemoveARecord() {
        assertFalse(service.deleteShop(99L));
        assertEquals(1, dao.writeCalls);
    }

    private static class RecordingShopDAO extends ShopDAO {
        private int writeCalls;
        private ShopInfo updateResult;

        RecordingShopDAO() {
            super(null);
        }

        @Override
        public ShopInfo updateShop(ShopInfo shopInfo) {
            writeCalls++;
            return updateResult;
        }

        @Override
        public boolean deleteShop(Long shopId) {
            writeCalls++;
            return false;
        }
    }
}
