package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ApiException;
import ru.practicum.shareit.exception.ErrorCode;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@mail.com");
        owner.setName("Owner");

        item = new Item();
        item.setId(10);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setOwner(1);
    }

    @Test
    void findItemsByName_whenTextBlank_returnsEmpty() {
        assertThat(itemService.findItemsByName(null)).isEmpty();
        assertThat(itemService.findItemsByName("")).isEmpty();
        assertThat(itemService.findItemsByName("   ")).isEmpty();

        verify(itemRepository, never()).findItemsByName(anyString());
    }

    @Test
    void findItemsByName_whenTextPresent_delegatesToRepo() {
        when(itemRepository.findItemsByName("дрель")).thenReturn(List.of(item));

        List<Item> result = itemService.findItemsByName("дрель");

        assertThat(result).containsExactly(item);
    }

    @Test
    void findAllOwnerItems_whenUserNotFound_throws() {
        when(userRepository.getById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findAllOwnerItems(99))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void findAllOwnerItems_whenUserExists_returnsItems() {
        when(userRepository.getById(1)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllOwnerItems(1)).thenReturn(List.of(item));

        List<Item> result = itemService.findAllOwnerItems(1);

        assertThat(result).containsExactly(item);
    }

    @Test
    void getById_whenNotFound_throws() {
        when(itemRepository.getById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(99))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    void save_whenOwnerExists_savesWithOwnerId() {
        when(userRepository.getById(1)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.save(1, item);

        assertThat(result.getOwner()).isEqualTo(1);
        verify(itemRepository).save(item);
    }

    @Test
    void save_whenOwnerNotFound_throws() {
        when(userRepository.getById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.save(1, item))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void update_whenNotOwner_throwsAccessDenied() {
        Item existing = new Item();
        existing.setId(10);
        existing.setOwner(2);

        when(itemRepository.getById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> itemService.update(1, 10, item))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(itemRepository, never()).update(any());
    }

    @Test
    void update_whenOwner_updatesOnlyProvidedFields() {
        Item existing = new Item();
        existing.setId(10);
        existing.setName("Old");
        existing.setDescription("Old Desc");
        existing.setAvailable(true);
        existing.setOwner(1);

        Item patch = new Item();
        patch.setName("New Name");

        when(itemRepository.getById(10)).thenReturn(Optional.of(existing));
        when(itemRepository.update(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.update(1, 10, patch);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("Old Desc");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void delete_whenExists_deletes() {
        when(itemRepository.getById(10)).thenReturn(Optional.of(item));

        itemService.delete(10);

        verify(itemRepository).delete(10);
    }
}