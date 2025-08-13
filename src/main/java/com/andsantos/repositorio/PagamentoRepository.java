package com.andsantos.repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.andsantos.model.Pagamento;
import com.andsantos.model.Resumo;

@Repository
public class PagamentoRepository {

    private final JdbcTemplate jdbcTemplate;

    public PagamentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void registrarPagamento(Pagamento pagamento, String processor) {
        String sql = "INSERT INTO PAYMENTS (ID, AMOUNT, REQUESTED_AT, PROCESSOR) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, pagamento.correlationId(), pagamento.amount(), pagamento.requestedAt(), processor);
    }

    public Resumo obterResumo(ZonedDateTime from, ZonedDateTime to) {
        Resumo resumo = new Resumo();

        String sql = """
                    SELECT PROCESSOR, COUNT(*) AS TOTAL, SUM(AMOUNT) SOMA
                    FROM PAYMENTS
                """;

        List<LocalDateTime> params = new ArrayList<>();
        String filtro = null;

        if (from != null && to != null) {
            filtro = " REQUESTED_AT BETWEEN ? AND ? ";
            params.add(from.toLocalDateTime());
            params.add(to.toLocalDateTime());
        } else if (from != null) {
            filtro = " REQUESTED_AT >= ? ";
            params.add(from.toLocalDateTime());
        } else if (to != null) {
            filtro = " REQUESTED_AT <= ? ";
            params.add(to.toLocalDateTime());
        }

        if (filtro != null) {
            sql += " WHERE " + filtro;
        }

        sql += " GROUP BY PROCESSOR";

        jdbcTemplate.query(sql.toString(), new RowMapper<Void>() {
            @Override
            public Void mapRow(@NonNull ResultSet rst, int rowNum) throws SQLException {
                if ("DEFAULT".equals(rst.getString("PROCESSOR"))) {
                    resumo.getPadrao().setTotalRequests(rst.getLong("TOTAL"));
                    resumo.getPadrao().setTotalAmount(rst.getBigDecimal("SOMA"));
                } else {
                    resumo.getFallback().setTotalRequests(rst.getLong("TOTAL"));
                    resumo.getFallback().setTotalAmount(rst.getBigDecimal("SOMA"));
                }
                return null;
            }
        }, params.toArray());

        return resumo;
    }

    @Transactional
    public void purge() {
        jdbcTemplate.update("TRUCATE TABLE PAYMENTS ");
    }
}
