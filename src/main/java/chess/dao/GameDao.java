package chess.dao;

import chess.dto.GameCountResponseDto;
import chess.dto.RoomResponseDto;
import chess.entity.Game;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class GameDao {

    private final JdbcTemplate jdbcTemplate;

    public GameDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public long insert(Game game) {
        final String sql = "INSERT INTO game(name, host_id, guest_id) VALUES (?, ?, ?)";
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final PreparedStatementCreator preparedStatementCreator = con -> {
            final PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, game.getName());
            preparedStatement.setLong(2, game.getHostId());
            preparedStatement.setLong(3, game.getGuestId());
            return preparedStatement;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Game findById(long gameId) {
        final String sql = "SELECT * FROM game WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, gameRowMapper, gameId);
    }

    public void updateGameStatus(long id, boolean isFinished) {
        String sql = "UPDATE game SET is_finished = ? WHERE id = ?";
        jdbcTemplate.update(sql, isFinished, id);
    }

    public void updateTurn(long id) {
        String sql = "UPDATE game SET turn = (CASE WHEN turn = 'BLACK' THEN 'WHITE' ELSE 'BLACK' END) WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public String getTurn(long id) {
        String sql = "SELECT turn FROM game WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public List<RoomResponseDto> getRooms() {
        String sql = "SELECT name, id FROM game WHERE is_finished = 0";
        return jdbcTemplate.query(sql, roomRowMapper);
    }

    public GameCountResponseDto gameCount() {
        String sql = "SELECT count(*) FROM game";
        return jdbcTemplate.queryForObject(sql, gameCountRowMapper);
    }

    private final RowMapper<GameCountResponseDto> gameCountRowMapper = (resultSet, rowNum) -> new GameCountResponseDto(
            resultSet.getInt("count(*)")
    );

    public int deleteByGameId(long roomId) {
        String sql = "DELETE FROM game WHERE id = ?";
        return jdbcTemplate.update(sql, roomId);
    }

    public int findByName(String roomName) {
        String sql = "SELECT EXISTS(SELECT * FROM game WHERE name = ?) as isChk";
        return jdbcTemplate.queryForObject(sql, Integer.class, roomName);
    }

    private final RowMapper<Game> gameRowMapper = (resultSet, rowNum) -> new Game(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getLong("host_id"),
            resultSet.getLong("guest_id"),
            resultSet.getString("turn"),
            resultSet.getBoolean("is_finished"),
            resultSet.getTimestamp("created_time").toLocalDateTime()
    );

    private final RowMapper<RoomResponseDto> roomRowMapper = (resultSet, rowNum) -> new RoomResponseDto(
            resultSet.getString("name"),
            resultSet.getLong("id")
    );
}
