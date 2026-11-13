/* ============================================================
   SPVSS — small progressive-enhancement script.
   Every page works without it; it adds:
     - dark / light theme toggle (saved in localStorage)
     - mobile navigation toggle
     - confirmation prompts for destructive forms (data-confirm)
     - same-sport filtering on the Compare form
     - live character counters on report text areas
   ============================================================ */
(function () {
    var root = document.documentElement;
    var STORAGE_KEY = 'spvss-theme';

    function currentTheme() {
        return root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
    }

    function syncToggleButtons() {
        var dark = currentTheme() === 'dark';
        var buttons = document.querySelectorAll('[data-theme-toggle]');
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].setAttribute('aria-pressed', dark ? 'true' : 'false');
            var label = dark ? 'Switch to light mode' : 'Switch to dark mode';
            buttons[i].setAttribute('aria-label', label);
            buttons[i].setAttribute('title', label);
        }
    }

    function setTheme(theme) {
        root.setAttribute('data-theme', theme);
        try { localStorage.setItem(STORAGE_KEY, theme); } catch (e) { /* storage blocked: theme still applies for this page */ }
        syncToggleButtons();
    }

    function initThemeToggle() {
        syncToggleButtons();
        document.addEventListener('click', function (event) {
            var button = event.target.closest('[data-theme-toggle]');
            if (!button) return;
            setTheme(currentTheme() === 'dark' ? 'light' : 'dark');
        });
    }

    function initNavToggle() {
        var toggle = document.querySelector('[data-nav-toggle]');
        if (!toggle) return;
        var bar = toggle.closest('.topbar');
        toggle.addEventListener('click', function () {
            var open = bar.classList.toggle('nav-open');
            toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        });
    }

    function initConfirmForms() {
        document.addEventListener('submit', function (event) {
            var form = event.target;
            var message = form.getAttribute && form.getAttribute('data-confirm');
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    }

    // Compare form: Player 2 only lists players from Player 1's sport
    function initCompareFilter() {
        var first = document.getElementById('player1Id');
        var second = document.getElementById('player2Id');
        if (!first || !second) return;
        var hint = document.getElementById('compare-hint');

        function refresh() {
            var selected = first.options[first.selectedIndex];
            var sport = selected ? selected.getAttribute('data-sport') : '';
            var visible = 0;
            for (var i = 0; i < second.options.length; i++) {
                var opt = second.options[i];
                if (!opt.value) continue;
                var allowed = !sport || (opt.getAttribute('data-sport') === sport && opt.value !== first.value);
                opt.hidden = !allowed;
                opt.disabled = !allowed;
                if (allowed) visible++;
            }
            var current = second.options[second.selectedIndex];
            if (current && current.disabled) second.value = '';
            second.disabled = !sport;
            if (hint) {
                hint.textContent = !sport
                    ? 'Choose the first player to unlock players from the same sport.'
                    : (visible === 0
                        ? 'No other ' + sport + ' players are available to compare.'
                        : 'Showing ' + visible + ' other ' + sport + ' player' + (visible === 1 ? '' : 's') + '.');
            }
        }
        first.addEventListener('change', refresh);
        refresh();
    }

    function initCounters() {
        var areas = document.querySelectorAll('textarea[maxlength]');
        for (var i = 0; i < areas.length; i++) {
            (function (area) {
                var counter = document.createElement('div');
                counter.className = 'char-counter';
                area.parentNode.insertBefore(counter, area.nextSibling);
                function update() {
                    counter.textContent = area.value.length + ' / ' + area.getAttribute('maxlength');
                }
                area.addEventListener('input', update);
                update();
            })(areas[i]);
        }
    }

    function ready(fn) {
        if (document.readyState !== 'loading') fn();
        else document.addEventListener('DOMContentLoaded', fn);
    }

    ready(function () {
        initThemeToggle();
        initNavToggle();
        initConfirmForms();
        initCompareFilter();
        initCounters();
    });
})();
