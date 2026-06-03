package com.focuszone.data.db;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<SessionEntity> __insertAdapterOfSessionEntity;

  private final EntityDeleteOrUpdateAdapter<SessionEntity> __deleteAdapterOfSessionEntity;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfSessionEntity = new EntityInsertAdapter<SessionEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sessions` (`id`,`type`,`durationSeconds`,`completedAt`,`wasSkipped`,`cycleNumber`,`sessionNumberInCycle`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final SessionEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getType());
        }
        statement.bindLong(3, entity.getDurationSeconds());
        statement.bindLong(4, entity.getCompletedAt());
        final int _tmp = entity.isWasSkipped() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCycleNumber());
        statement.bindLong(7, entity.getSessionNumberInCycle());
      }
    };
    this.__deleteAdapterOfSessionEntity = new EntityDeleteOrUpdateAdapter<SessionEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final SessionEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public void insertSession(final SessionEntity session) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfSessionEntity.insert(_connection, session);
      return null;
    });
  }

  @Override
  public void deleteSession(final SessionEntity session) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __deleteAdapterOfSessionEntity.handle(_connection, session);
      return null;
    });
  }

  @Override
  public LiveData<List<SessionEntity>> getAllSessions() {
    final String _sql = "SELECT `sessions`.`id` AS `id`, `sessions`.`type` AS `type`, `sessions`.`durationSeconds` AS `durationSeconds`, `sessions`.`completedAt` AS `completedAt`, `sessions`.`wasSkipped` AS `wasSkipped`, `sessions`.`cycleNumber` AS `cycleNumber`, `sessions`.`sessionNumberInCycle` AS `sessionNumberInCycle` FROM sessions ORDER BY completedAt DESC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = 0;
        final int _columnIndexOfType = 1;
        final int _columnIndexOfDurationSeconds = 2;
        final int _columnIndexOfCompletedAt = 3;
        final int _columnIndexOfWasSkipped = 4;
        final int _columnIndexOfCycleNumber = 5;
        final int _columnIndexOfSessionNumberInCycle = 6;
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<SessionEntity>> getFocusSessions() {
    final String _sql = "SELECT `sessions`.`id` AS `id`, `sessions`.`type` AS `type`, `sessions`.`durationSeconds` AS `durationSeconds`, `sessions`.`completedAt` AS `completedAt`, `sessions`.`wasSkipped` AS `wasSkipped`, `sessions`.`cycleNumber` AS `cycleNumber`, `sessions`.`sessionNumberInCycle` AS `sessionNumberInCycle` FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt DESC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = 0;
        final int _columnIndexOfType = 1;
        final int _columnIndexOfDurationSeconds = 2;
        final int _columnIndexOfCompletedAt = 3;
        final int _columnIndexOfWasSkipped = 4;
        final int _columnIndexOfCycleNumber = 5;
        final int _columnIndexOfSessionNumberInCycle = 6;
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<SessionEntity>> getBreakSessions() {
    final String _sql = "SELECT `sessions`.`id` AS `id`, `sessions`.`type` AS `type`, `sessions`.`durationSeconds` AS `durationSeconds`, `sessions`.`completedAt` AS `completedAt`, `sessions`.`wasSkipped` AS `wasSkipped`, `sessions`.`cycleNumber` AS `cycleNumber`, `sessions`.`sessionNumberInCycle` AS `sessionNumberInCycle` FROM sessions WHERE type IN ('SHORT_BREAK', 'LONG_BREAK') AND wasSkipped = 0 ORDER BY completedAt DESC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = 0;
        final int _columnIndexOfType = 1;
        final int _columnIndexOfDurationSeconds = 2;
        final int _columnIndexOfCompletedAt = 3;
        final int _columnIndexOfWasSkipped = 4;
        final int _columnIndexOfCycleNumber = 5;
        final int _columnIndexOfSessionNumberInCycle = 6;
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<SessionEntity>> getSessionsToday(final long startOfDay) {
    final String _sql = "SELECT * FROM sessions WHERE completedAt >= ? AND wasSkipped = 0 ORDER BY completedAt DESC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startOfDay);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "type");
        final int _columnIndexOfDurationSeconds = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSeconds");
        final int _columnIndexOfCompletedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "completedAt");
        final int _columnIndexOfWasSkipped = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "wasSkipped");
        final int _columnIndexOfCycleNumber = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "cycleNumber");
        final int _columnIndexOfSessionNumberInCycle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sessionNumberInCycle");
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getFocusCountToday(final long startOfDay) {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startOfDay);
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getFocusSecondsToday(final long startOfDay) {
    final String _sql = "SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startOfDay);
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<Integer> getFocusCountTodayLiveData(final long startOfDay) {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= ?";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startOfDay);
        final Integer _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp;
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<Integer> getFocusSecondsTodayLiveData(final long startOfDay) {
    final String _sql = "SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt >= ?";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startOfDay);
        final Integer _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp;
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<Long>> getAllFocusTimestampsLiveData() {
    final String _sql = "SELECT completedAt FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt ASC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"sessions"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final List<Long> _result = new ArrayList<Long>();
        while (_stmt.step()) {
          final Long _item;
          if (_stmt.isNull(0)) {
            _item = null;
          } else {
            _item = _stmt.getLong(0);
          }
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getTotalFocusSeconds() {
    final String _sql = "SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getTotalBreakSeconds() {
    final String _sql = "SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type IN ('SHORT_BREAK', 'LONG_BREAK') AND wasSkipped = 0";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getTotalCompletedSessions() {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE wasSkipped = 0";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getTotalFocusSessions() {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getTotalActiveDays() {
    final String _sql = "SELECT COUNT(DISTINCT date(completedAt/1000, 'unixepoch', 'localtime')) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public List<Long> getAllFocusTimestamps() {
    final String _sql = "SELECT completedAt FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 ORDER BY completedAt ASC";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final List<Long> _result = new ArrayList<Long>();
        while (_stmt.step()) {
          final Long _item;
          if (_stmt.isNull(0)) {
            _item = null;
          } else {
            _item = _stmt.getLong(0);
          }
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public List<SessionEntity> getAllSessionsBlocking() {
    final String _sql = "SELECT `sessions`.`id` AS `id`, `sessions`.`type` AS `type`, `sessions`.`durationSeconds` AS `durationSeconds`, `sessions`.`completedAt` AS `completedAt`, `sessions`.`wasSkipped` AS `wasSkipped`, `sessions`.`cycleNumber` AS `cycleNumber`, `sessions`.`sessionNumberInCycle` AS `sessionNumberInCycle` FROM sessions WHERE wasSkipped = 0 ORDER BY completedAt DESC";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = 0;
        final int _columnIndexOfType = 1;
        final int _columnIndexOfDurationSeconds = 2;
        final int _columnIndexOfCompletedAt = 3;
        final int _columnIndexOfWasSkipped = 4;
        final int _columnIndexOfCycleNumber = 5;
        final int _columnIndexOfSessionNumberInCycle = 6;
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public List<SessionEntity> getSessionsBetween(final long startMillis, final long endMillis) {
    final String _sql = "SELECT * FROM sessions WHERE completedAt BETWEEN ? AND ? AND wasSkipped = 0 ORDER BY completedAt ASC";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startMillis);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, endMillis);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "type");
        final int _columnIndexOfDurationSeconds = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationSeconds");
        final int _columnIndexOfCompletedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "completedAt");
        final int _columnIndexOfWasSkipped = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "wasSkipped");
        final int _columnIndexOfCycleNumber = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "cycleNumber");
        final int _columnIndexOfSessionNumberInCycle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sessionNumberInCycle");
        final List<SessionEntity> _result = new ArrayList<SessionEntity>();
        while (_stmt.step()) {
          final SessionEntity _item;
          final String _tmpType;
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmpType = null;
          } else {
            _tmpType = _stmt.getText(_columnIndexOfType);
          }
          final int _tmpDurationSeconds;
          _tmpDurationSeconds = (int) (_stmt.getLong(_columnIndexOfDurationSeconds));
          final long _tmpCompletedAt;
          _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt);
          final boolean _tmpWasSkipped;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfWasSkipped));
          _tmpWasSkipped = _tmp != 0;
          final int _tmpCycleNumber;
          _tmpCycleNumber = (int) (_stmt.getLong(_columnIndexOfCycleNumber));
          final int _tmpSessionNumberInCycle;
          _tmpSessionNumberInCycle = (int) (_stmt.getLong(_columnIndexOfSessionNumberInCycle));
          _item = new SessionEntity(_tmpType,_tmpDurationSeconds,_tmpCompletedAt,_tmpWasSkipped,_tmpCycleNumber,_tmpSessionNumberInCycle);
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getFocusCountBetween(final long startMillis, final long endMillis) {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt BETWEEN ? AND ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startMillis);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, endMillis);
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getFocusSecondsBetween(final long startMillis, final long endMillis) {
    final String _sql = "SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND completedAt BETWEEN ? AND ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, startMillis);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, endMillis);
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getFocusCountForCycle(final int cycleNumber) {
    final String _sql = "SELECT COUNT(*) FROM sessions WHERE type = 'FOCUS' AND wasSkipped = 0 AND cycleNumber = ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cycleNumber);
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public void deleteAllSessions() {
    final String _sql = "DELETE FROM sessions";
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return null;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
