package com.andsantos.repositorio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.andsantos.model.Pagamento;

@Repository
public class PagamentoRepository {

    private final JdbcTemplate jdbcTemplate;

    public PagamentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void registrarPagamentoPadrao(Pagamento pagamento) {
        String sql = "INSERT INTO PAYMENTS_DEFAULT (ID, AMOUNT, REQUESTED_AT) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, pagamento.id(), pagamento.amount(), pagamento.requestedAt());
    }

    @Transactional
    public void registrarPagamentoContingencia(Pagamento pagamento) {
        String sql = "INSERT INTO PAYMENTS_FALLBACK (ID, AMOUNT, REQUESTED_AT) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, pagamento.id(), pagamento.amount(), pagamento.requestedAt());
    }

}
