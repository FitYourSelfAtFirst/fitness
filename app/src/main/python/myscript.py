import re
import data as d

# --- Получение списков для спиннеров ---

def get_ages(gender):
    """Вернуть список возрастных групп для пола"""
    return list(d.age_groups[gender].keys())

def get_strength_exercises(gender):
    """Вернуть список упражнений на силу для пола"""
    return list(d.exercises["сила"][gender].keys())

def get_speed_exercises(gender):
    """Вернуть список упражнений на быстроту для пола"""
    return list(d.exercises["быстрота"][gender].keys())

def get_endurance_exercises(gender):
    """Вернуть список упражнений на выносливость для пола"""
    return list(d.exercises["выносливость"][gender].keys())

def get_exercise_range(gender, category, exercise_name, age=None):
    """
    Вернуть минимальное и максимальное значение для упражнения (для подсказки в поле ввода)
    Если это "Бег 1 км", диапазон зависит от возраста!
    """
    score_table = d.exercises[category][gender][exercise_name]
    keys = [k for k in score_table if is_valid_time_key(k)]
    if not keys:
        return {"min": "", "max": ""}

    # Особая логика для "Бег 1 км"
    if category == "выносливость" and exercise_name == "Бег 1 км" and age is not None:
        age_range = get_age_range_for_1km(age)
        if age_range == 1:
            min_time, max_time = "1.00", "3.09"
        elif age_range == 2:
            min_time, max_time = "3.10", "3.23"
        else:
            min_time, max_time = keys[0], keys[-1]
        return {"min": min_time, "max": max_time}

    is_time = any(':' in str(k) for k in keys)
    if is_time:
        sorted_keys = sorted(keys, key=lambda x: time_to_seconds(x))
    else:
        sorted_keys = sorted(keys, key=lambda x: float(x))
    return {"min": sorted_keys[0], "max": sorted_keys[-1]}

# --- Вспомогательные функции ---

def get_age_range_for_1km(age):
    """Определяет диапазон для расчета 'Бег 1 км' по возрасту"""
    range1 = {"до 25 лет", "25–30 лет", "30–35 лет"}
    range2 = {"35–40 лет", "40–45 лет", "45–50 лет", "50–55 лет", "55+ лет"}
    if age in range1:
        return 1
    elif age in range2:
        return 2
    else:
        return None

def time_to_seconds(t):
    s = str(t).strip().replace(',', '.')
    if s == '-' or s == '':
        return float('inf')
    if ':' in s:
        try:
            mins, secs = s.split(':')
            return float(mins) * 60 + float(secs)
        except (ValueError, TypeError):
            return float('inf')
    if re.match(r'^\d+\.\d+$', s):
        try:
            mins, secs = s.split('.')
            return float(mins) * 60 + float(secs)
        except (ValueError, TypeError):
            return float('inf')
    try:
        return float(s)
    except (ValueError, TypeError):
        return float('inf')

def is_valid_time_key(k):
    s = str(k).strip().replace(',', '.')
    if s == '-' or s == '':
        return False
    if ':' in s:
        parts = s.split(':')
        if len(parts) == 2:
            try:
                float(parts[0])
                float(parts[1])
                return True
            except (ValueError, TypeError):
                return False
        else:
            return False
    if re.match(r'^\d+\.\d+$', s):
        try:
            float(s.split('.')[0])
            float(s.split('.')[1])
            return True
        except (ValueError, TypeError):
            return False
    try:
        float(s)
        return True
    except (ValueError, TypeError):
        return False

def parse_value(value):
    s = str(value).strip().replace(',', '.')
    if s == '-' or s == '':
        return float('inf')
    if ':' in s:
        return time_to_seconds(s)
    if re.match(r'^\d+\.\d+$', s):
        return time_to_seconds(s)
    try:
        return float(s)
    except (ValueError, TypeError):
        return float('inf')

def get_ball(score_table, value, min_time=None, max_time=None):
    try:
        is_time = False
        for k in score_table:
            s = str(k)
            if ':' in s or re.match(r'^\d+\.\d+$', s) or (s.isdigit() and len(s) <= 3):
                is_time = True
                break
        value_sec = parse_value(value)
        if is_time:
            keys = [k for k in score_table if is_valid_time_key(k)]
            if not keys:
                return 0
            keys_sorted = sorted(keys, key=lambda x: time_to_seconds(x))
            # Фильтрация по диапазону, если задан
            if min_time is not None and max_time is not None:
                min_sec = time_to_seconds(min_time)
                max_sec = time_to_seconds(max_time)
                keys_sorted = [k for k in keys_sorted if min_sec <= time_to_seconds(k) <= max_sec]
                if not keys_sorted:
                    return 0
            for k in keys_sorted:
                if value_sec <= time_to_seconds(k):
                    return score_table[k]
            return score_table[keys_sorted[-1]]
        else:
            keys = [k for k in score_table if is_valid_time_key(k)]
            if not keys:
                return 0
            keys_sorted = sorted(keys, key=lambda x: float(x))
            result = 0
            for k in keys_sorted:
                if value_sec >= float(k):
                    result = score_table[k]
            return result
    except (ValueError, TypeError, AttributeError):
        return 0

# --- Основная функция для расчёта итоговой оценки ---

def calculate_all(gender, age, strength_ex, v_s, speed_ex, v_f, endurance_ex, v_e):
    try:
        group = d.age_groups[gender][age]
        threshold = group["threshold"]
        grades = group["grades"]

        # --- Особая логика для "Бег 1 км" ---
        if endurance_ex == "Бег 1 км":
            age_range = get_age_range_for_1km(age)
            if age_range == 1:
                min_time = "1.00"
                max_time = "3.09"
            elif age_range == 2:
                min_time = "3.10"
                max_time = "3.23"
            else:
                min_time = None
                max_time = None
            b_e = get_ball(d.exercises["выносливость"][gender][endurance_ex], v_e, min_time, max_time)
        else:
            b_e = get_ball(d.exercises["выносливость"][gender][endurance_ex], v_e)

        b_s = get_ball(d.exercises["сила"][gender][strength_ex], v_s)
        b_f = get_ball(d.exercises["быстрота"][gender][speed_ex], v_f)

        points = [b_s, b_f, b_e]
        total = sum(points)

        if min(points) < threshold:
            mark = "неудовлетворительно"
        elif total >= grades.get("кв", 9999):
            mark = "отлично (высший квалификационный уровень)"
        elif total >= grades.get("к1", 9999):
            mark = "отлично (1 квалификационный уровень)"
        elif total >= grades.get("к2", 9999):
            mark = "отлично (2 квалификационный уровень)"
        elif total >= grades.get("отл", 9999):
            mark = "отлично"
        elif total >= grades.get("хор", 9999):
            mark = "хорошо"
        elif total >= grades.get("уд", 9999):
            mark = "удовлетворительно"
        else:
            mark = "неудовлетворительно"

        return {
            "strength_points": b_s,
            "speed_points": b_f,
            "endurance_points": b_e,
            "total": total,
            "mark": mark
        }
    except Exception as e:
        return {"error": str(e)}
