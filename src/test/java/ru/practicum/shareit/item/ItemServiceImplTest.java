package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
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

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        item = Item.builder()
                .id(10L)
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .owner(owner)
                .build();

        itemDto = ItemDto.builder()
                .id(10L)
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .build();
    }

    @Test
    void search_whenTextBlank_returnsEmpty() {
        assertThat(itemService.search(null)).isEmpty();
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search("   ")).isEmpty();

        verify(itemRepository, never()).searchAvailableByText(anyString());
    }

    @Test
    void search_whenTextPresent_delegatesToRepo() {
        when(itemRepository.searchAvailableByText("дрель")).thenReturn(List.of(item));
        when(itemMapper.toDtoList(List.of(item))).thenReturn(List.of(itemDto));

        List<ItemDto> result = itemService.search("дрель");

        assertThat(result).containsExactly(itemDto);
    }

    @Test
    void getAllByOwner_whenUserNotFound_throws() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getAllByOwner(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllByOwner_whenUserExists_returnsItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of(item));
        when(itemMapper.toDtoList(List.of(item))).thenReturn(List.of(itemDto));

        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertThat(result).containsExactly(itemDto);
    }

    @Test
    void getById_whenNotFound_throws() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_whenFound_returnsDto() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.getById(1L, 10L);

        assertThat(result).isEqualTo(itemDto);
    }

    @Test
    void create_whenOwnerExists_saves() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(1L, request);

        assertThat(result).isEqualTo(itemDto);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_whenOwnerNotFound_throws() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Аккумуляторная");
        request.setAvailable(true);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(99L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_whenNotOwner_throwsNotFound() {
        User otherOwner = User.builder().id(2L).build();
        Item existing = Item.builder()
                .id(10L)
                .owner(otherOwner)
                .build();

        when(itemRepository.findById(10L)).thenReturn(Optional.of(existing));

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        assertThatThrownBy(() -> itemService.update(1L, 10L, request))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_whenOwner_updatesOnlyProvidedFields() {
        Item existing = Item.builder()
                .id(10L)
                .name("Old")
                .description("Old Desc")
                .available(true)
                .owner(owner)
                .build();

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("New Name");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemMapper.toDto(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            return ItemDto.builder()
                    .id(i.getId())
                    .name(i.getName())
                    .description(i.getDescription())
                    .available(i.getAvailable())
                    .build();
        });

        ItemDto result = itemService.update(1L, 10L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("Old Desc");
        assertThat(result.getAvailable()).isTrue();
    }
}